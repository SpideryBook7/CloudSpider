import base64

data_server = "cDI3Q25sMng4M2RlSm00aUR2WmJGaFRNVnFxZnlBWHc5b1NQWnp3MC9PWENycnBNcU5vPQ=="
encoded = base64.b64encode(data_server.encode('utf-8')).decode('utf-8')
full_url = f"https://tioplus.app/player/{encoded}"
print(f"Original: {data_server}")
print(f"Encoded: {encoded}")
print(f"Full URL: {full_url}")

with open("url.txt", "w") as f:
    f.write(full_url)
