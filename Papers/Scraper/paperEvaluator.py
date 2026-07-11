import json
import time
import requests
import pandas as pd
from tqdm import tqdm

#local connection
OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = "llama3.2:latest"


def score_paper(title, abstract, citations):
    safe_abstract = abstract if pd.notna(abstract) and str(abstract).strip() else "No abstract provided."

    prompt = f"""
    Evaluate this research paper's relevancy (0-10) for a project on Metaheuristics and Graph Coloring. Prioritize graph coloring and metaheuristic relavance, with  a slightly less but still significant weight for citation number
    Title: {title}
    Abstract: {safe_abstract}
    Citations: {citations}
    Return ONLY JSON: {{"score": <int>, "reasoning": "<string>"}}
    """

    payload = {
        "model": MODEL_NAME,
        "prompt": prompt,
        "stream": False,
        "format": "json",
        "options": {
            "temperature": 0.1
        }
    }

    try:
        response = requests.post(OLLAMA_URL, json=payload, timeout=120)
        response.raise_for_status()

        raw = response.json().get("response", "")
        parsed = json.loads(raw)
        return {
            "score": int(parsed.get("score", 0)),
            "reasoning": parsed.get("reasoning", "No reasoning returned.")
        }

    except requests.exceptions.ConnectionError:
        print("\n[!] CONNECTION ERROR: Ollama is not running. Start it with: ollama serve")
        return {"score": 0, "reasoning": "Ollama not running"}
    except requests.exceptions.Timeout:
        print("\n[!] TIMEOUT: Model took too long to respond.")
        return {"score": 0, "reasoning": "Request timed out"}
    except (json.JSONDecodeError, KeyError, ValueError) as e:
        print(f"\n[!] PARSE ERROR: Could not parse model response — {e}")
        return {"score": 0, "reasoning": f"Parse error: {str(e)[:80]}"}
    except Exception as e:
        print(f"\n[!] UNEXPECTED ERROR: {e}")
        return {"score": 0, "reasoning": f"Unexpected error: {str(e)[:80]}"}


#actual run
def main():
    df = pd.read_csv("graph_coloring_heuristics.csv")
    total = len(df)

    results = []
    print(f"Starting full run: processing all {total} papers...")
    print(f"Model: {MODEL_NAME}  |  Endpoint: {OLLAMA_URL}\n")

    for i, row in tqdm(df.iterrows(), total=total):
        res = score_paper(row['title'], row['abstract'], row['citations'])

        full_row = row.to_dict()
        full_row['relevancy_score'] = res.get('score', 0)
        full_row['reasoning'] = res.get('reasoning', "N/A")
        results.append(full_row)

        # checkpoint 
        if (len(results) % 50) == 0:
            pd.DataFrame(results).to_csv("papers_scored.csv", index=False)
            print(f"\n[CHECKPOINT] {len(results)}/{total} papers saved.")

        time.sleep(0.5)

    # Final write
    output_df = pd.DataFrame(results)
    output_df.to_csv("papers_scored.csv", index=False)
    print(f"\nFull run complete. All {total} papers written to 'papers_scored.csv'.")


if __name__ == "__main__":
    main()