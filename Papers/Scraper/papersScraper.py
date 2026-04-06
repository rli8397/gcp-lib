import requests
import pandas as pd
import time
from pathlib import Path

API_KEY = "KyyWkDIIu4781tWqGhSB29PYM19UY0vp9SGploXW"  # optional
headers = {"x-api-key": API_KEY} if API_KEY else {}

queries = [
    "graph coloring heuristic",
]

url = "https://api.semanticscholar.org/graph/v1/paper/search"
output_csv_path = Path(__file__).resolve().parent / "graph_coloring_heuristics.csv"
max_papers = 500
page_size = 100

papers = []

for query in queries:
    offset = 0

    while len(papers) < max_papers:
        params = {
            "query": query,
            "limit": min(page_size, max_papers - len(papers)),
            "offset": offset,
            "fields": "title,authors,year,venue,abstract,citationCount,url"
        }

        response = requests.get(url, params=params, headers=headers)

        if response.status_code != 200:
            print(f"Request failed for query '{query}' with status {response.status_code}: {response.text[:500]}")
            break

        data = response.json()
        batch = data.get("data", [])

        if not batch:
            break

        for paper in batch:
            papers.append({
                "title": paper.get("title"),
                "authors": ", ".join([a["name"] for a in paper.get("authors", [])]),
                "year": paper.get("year"),
                "venue": paper.get("venue"),
                "citations": paper.get("citationCount"),
                "url": paper.get("url"),
                "abstract": paper.get("abstract")
            })

        offset += len(batch)
        time.sleep(1)

df = pd.DataFrame(
    papers,
    columns=["title", "authors", "year", "venue", "citations", "url", "abstract"]
)

if not df.empty:
    df = df.drop_duplicates(subset="title")
    df.sort_values(by="citations", ascending=False, inplace=True)
    df = df[df['citations'] > 0]
df.to_csv(output_csv_path, index=False)
print(f"Saved {len(df)} rows to {output_csv_path}")