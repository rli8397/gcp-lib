import requests
import pandas as pd
import time
import random
from pathlib import Path
from bs4 import BeautifulSoup

API_KEY = "KyyWkDIIu4781tWqGhSB29PYM19UY0vp9SGploXW"
headers = {"x-api-key": API_KEY} if API_KEY else {}

queries = ["graph coloring heuristic"]

search_url = "https://api.semanticscholar.org/graph/v1/paper/search"
paper_url_template = "https://api.semanticscholar.org/graph/v1/paper/{}"

output_csv_path = Path(__file__).resolve().parent / "graph_coloring_heuristics.csv"

max_papers = 500
page_size = 100

# ---- RATE CONTROL ----
BASE_DELAY = 0.6          # baseline delay between requests
DETAIL_DELAY = 0.4        # delay for fallback API calls
SCRAPE_DELAY = 0.6        # delay for scraping
MAX_RETRIES = 6

papers = []

stats = {
    "total_processed": 0,
    "abstract_from_search": 0,
    "abstract_from_details": 0,
    "abstract_from_scrape": 0,
    "missing_abstract": 0,
    "rate_limit_hits": 0
}

# ----------- SAFE REQUEST -----------

def safe_request(url, params=None):
    for attempt in range(MAX_RETRIES):
        try:
            response = requests.get(url, params=params, headers=headers, timeout=15)

            if response.status_code == 200:
                time.sleep(BASE_DELAY + random.uniform(0, 0.3))
                return response

            elif response.status_code == 429:
                stats["rate_limit_hits"] += 1
                wait = (2 ** attempt) + random.uniform(0.5, 1.5)
                print(f"⏳ 429 hit. Backing off for {wait:.2f}s...")
                time.sleep(wait)

            else:
                print(f"❌ Error {response.status_code}: {response.text[:200]}")
                return None

        except requests.exceptions.RequestException as e:
            wait = (2 ** attempt)
            print(f"⚠️ Network error: {e}. Retrying in {wait}s...")
            time.sleep(wait)

    print("❌ Max retries exceeded.")
    return None

# ----------- FALLBACKS -----------

def get_paper_details(paper_id):
    url = paper_url_template.format(paper_id)
    params = {"fields": "abstract"}

    r = safe_request(url, params)
    if r:
        abstract = r.json().get("abstract")
        if abstract:
            time.sleep(DETAIL_DELAY)
        return abstract
    return None


def scrape_abstract_from_url(paper_url):
    try:
        r = requests.get(paper_url, timeout=10)
        soup = BeautifulSoup(r.text, "html.parser")

        meta = soup.find("meta", attrs={"name": "description"})
        if meta and meta.get("content"):
            time.sleep(SCRAPE_DELAY)
            return meta.get("content")

        for p in soup.find_all("p"):
            if "abstract" in p.text.lower():
                time.sleep(SCRAPE_DELAY)
                return p.text.strip()

    except Exception as e:
        print(f"   ⚠️ Scraping failed: {e}")

    return None

# ----------- MAIN -----------

print("🚀 Starting scraper...\n")

for query in queries:
    print(f"🔍 Query: '{query}'")
    offset = 0
    batch_num = 1

    while len(papers) < max_papers:
        print(f"\n📦 Fetching batch {batch_num} (offset={offset})")

        params = {
            "query": query,
            "limit": min(page_size, max_papers - len(papers)),
            "offset": offset,
            "fields": "paperId,title,authors,year,venue,abstract,citationCount,url"
        }

        response = safe_request(search_url, params)

        if not response:
            print("❌ Skipping batch due to repeated failure.")
            continue

        data = response.json()
        batch = data.get("data", [])

        if not batch:
            print("⚠️ No more results.")
            break

        print(f"   Retrieved {len(batch)} papers")

        for paper in batch:
            stats["total_processed"] += 1
            title = paper.get("title", "Unknown title")

            if stats["total_processed"] % 10 == 0:
                print(f"   ➤ Processed {stats['total_processed']} papers")

            abstract = paper.get("abstract")

            if abstract:
                stats["abstract_from_search"] += 1

            # ---- FALLBACK 1 (ONLY if worthwhile) ----
            if not abstract and paper.get("paperId") and paper.get("citationCount", 0) > 5:
                print(f"   🔄 Details API: {title[:60]}")
                abstract = get_paper_details(paper["paperId"])
                if abstract:
                    stats["abstract_from_details"] += 1

            # ---- FALLBACK 2 (RARE) ----
            if not abstract and paper.get("url") and paper.get("citationCount", 0) > 20:
                print(f"   🌐 Scraping: {title[:60]}")
                abstract = scrape_abstract_from_url(paper["url"])
                if abstract:
                    stats["abstract_from_scrape"] += 1

            if not abstract:
                stats["missing_abstract"] += 1

            papers.append({
                "title": title,
                "authors": ", ".join([a["name"] for a in paper.get("authors", [])]),
                "year": paper.get("year"),
                "venue": paper.get("venue"),
                "citations": paper.get("citationCount"),
                "url": paper.get("url"),
                "abstract": abstract
            })

        offset += len(batch)
        batch_num += 1

        print("\n📊 Progress Summary:")
        print(f"   Total processed: {stats['total_processed']}")
        print(f"   From search: {stats['abstract_from_search']}")
        print(f"   From details: {stats['abstract_from_details']}")
        print(f"   From scraping: {stats['abstract_from_scrape']}")
        print(f"   Missing: {stats['missing_abstract']}")
        print(f"   429 hits: {stats['rate_limit_hits']}")

# ----------- SAVE -----------

print("\n🧹 Cleaning data...")

df = pd.DataFrame(
    papers,
    columns=["title", "authors", "year", "venue", "citations", "url", "abstract"]
)

if not df.empty:
    df = df.drop_duplicates(subset="title")
    df = df.sort_values(by="citations", ascending=False)
    df = df[df['citations'] > 0]

df.to_csv(output_csv_path, index=False)

print("\n✅ Done!")
print(f"Saved {len(df)} rows to {output_csv_path}")