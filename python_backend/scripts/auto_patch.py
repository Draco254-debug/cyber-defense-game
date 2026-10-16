# python_backend/scripts/auto_patch.py

def main(api):
    print("[*] Script scanning nodes...")
    nodes = api.get_nodes()
    patched_count = 0
    
    # Iterate through all network variables looking for active anomalies
    for ip, data in nodes.items():
        if data["status"] == "UNDER_ATTACK":
            print(f"[*] Automation detected intrusion on {ip}. Triggering patch...")
            api.mitigate(ip)
            patched_count += 1
            
    return f"Successfully neutralized {patched_count} network threats automatically."
