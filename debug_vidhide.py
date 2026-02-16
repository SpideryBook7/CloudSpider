import re

def unpack(p, a, c, k, e=None, d=None):
    def baseN(num, b):
        if num == 0: return "0"
        digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (baseN(num // b, b).lstrip("0") if num >= b else "") + digits[num % b]

    # Convert k to a dictionary for faster/correct lookup if needed, 
    # but standard eval(function(p,a,c,k,e,d)... logic iterates:
    # while(c--) if(k[c]) p=p.replace(new RegExp('\\b'+c.toString(a)+'\\b','g'),k[c])
    
    for i in range(c - 1, -1, -1):
        if i < len(k) and k[i]:
            val = k[i]
            key = baseN(i, a)
            # Use word boundary
            p = re.sub(r'\b' + re.escape(key) + r'\b', val, p)
    return p

with open("temp_vidhide.html", "r", encoding="utf-8") as f:
    content = f.read()

match = re.search(r"eval\(function\(p,a,c,k,e,d\)\{.*\}\('(.*?)',(\d+),(\d+),'(.*?)'\.split\('\|'\)", content)

if match:
    p = match.group(1)
    a = int(match.group(2))
    c = int(match.group(3))
    k = match.group(4).split('|')
    
    unpacked = unpack(p, a, c, k)
    
    with open("unpacked.js", "w", encoding="utf-8") as out:
        out.write(unpacked)
    
    print("Unpacked content saved to unpacked.js")
    
    # Check for file
    match_file = re.search(r'file\s*:\s*"(https?://.*?)"', unpacked)
    if match_file:
        print(f"FOUND FILE: {match_file.group(1)}")
    else:
        print("NO 'file' found.")

    # Check for sources
    match_sources = re.search(r'sources\s*:\s*(\[.*?\])', unpacked)
    if match_sources:
        print(f"FOUND SOURCES: {match_sources.group(1)}")
else:
    print("No packed code found.")
