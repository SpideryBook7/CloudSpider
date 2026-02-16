import re
import json

with open("unpacked.js", "r", encoding="utf-8") as f:
    content = f.read()

match = re.search(r"var links\s*=\s*(\{.*?\});", content)
if match:
    json_str = match.group(1)
    # The JS object might not be valid JSON (keys without quotes?), but in this file they seem to have quotes.
    # Let's try to parse it loosely or use regex.
    print("Links object found:", json_str)
    
    urls = re.findall(r'"(hls\d+)":"(.*?)"', json_str)
    for key, url in urls:
        print(f"\nType: {key}")
        print(f"URL: {url}")
        
        full_url = url
        if url.startswith("/"):
            full_url = "https://vidhideplus.com" + url
        
        print(f"Full URL: {full_url}")
        
        # Write to file for curl
        with open(f"url_{key}.txt", "w") as out:
            out.write(full_url)
else:
    print("No links object found")
