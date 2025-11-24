from fastapi import FastAPI
import requests
from bs4 import BeautifulSoup

app = FastAPI()


def fetch_amazon_sde_jobs():
    base_url = "https://www.amazon.jobs/en/search.json"

    params = {
        "base_query": "software development engineer",
        "loc_query": "India",
        "country": "IND",
        "result_limit": 50,
        "offset": 0
    }

    all_jobs = []

    while True:
        resp = requests.get(base_url, params=params, headers={"User-Agent": "Mozilla/5.0"})
        if resp.status_code != 200:
            break

        data = resp.json()
        jobs = data.get("jobs", [])
        if not jobs:
            break

        for job in jobs:
            job_id = job.get("job_id") or job.get("id")
            title = job.get("title")
            url = job.get("job_url") or job.get("url")

            # fetch description
            description = ""
            if url:
                try:
                    detail = requests.get(url, headers={"User-Agent": "Mozilla/5.0"})
                    soup = BeautifulSoup(detail.text, "html.parser")
                    desc = soup.select_one(".job-description")
                    if desc:
                        description = desc.get_text("\n", strip=True)
                except:
                    pass

            all_jobs.append({
                "job_id": job_id,
                "title": title,
                "description": description,
                "url": url
            })

        params["offset"] += params["result_limit"]

    return all_jobs


@app.get("/scrape-amazon-jobs")
def scrape_jobs():
    results = fetch_amazon_sde_jobs()
    return {"count": len(results), "jobs": results}