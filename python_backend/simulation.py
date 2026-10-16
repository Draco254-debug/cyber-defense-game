import socket
import json
import threading
import time
import random
import os
import importlib.util

# --- EXPANDED GAME ENGINE DATABASE ---
network_nodes = {
    "192.168.1.10": {"name": "Firewall_Gateway", "status": "SECURE", "traffic": 10, "os": "SecureOS v4.2", "vuln": "None"},
    "192.168.1.20": {"name": "Web_Server", "status": "SECURE", "traffic": 15, "os": "Ubuntu 20.04 LTS", "vuln": "HTTP-Header-Exploit"},
    "192.168.1.30": {"name": "Auth_Service", "status": "SECURE", "traffic": 12, "os": "Debian Buster", "vuln": "SSH-BruteForce-Weakness"},
    "192.168.1.50": {"name": "Main_Database", "status": "SECURE", "traffic": 8, "os": "EnterpriseDB 15", "vuln": "SQL-Injection-Flaw"},
    "10.0.4.11":    {"name": "Honeypot_Node", "status": "SECURE", "traffic": 5, "os": "TrapNet v1.0", "vuln": "Deliberate-BufferOverflow"}
}

game_metrics = {
    "global_integrity": 100,
    "system_clock": 0,
    "current_wave": 1,
    "pending_narrative": None
}

SCRIPTS_DIR = os.path.join(os.path.dirname(__file__), "scripts")

# --- UPGRADE A & C: CAMPAIGN LOGIC & ATTACK ADVANCEMENTS ---
def game_master_loop():
    """Runs the real-time background ticks, launches attacks, and calculates global integrity."""
    time.sleep(2) # Brief boot buffer
    game_metrics["pending_narrative"] = "[HQ DIRECTIVE]: Systems online. Hackers from 'NullSector' are profiling our subnet. Keep ports clear!"

    while True:
        time.sleep(1)
        game_metrics["system_clock"] += 1
        elapsed = game_metrics["system_clock"]

        # Calculate overall system integrity based on compromised nodes
        compromised_count = sum(1 for n in network_nodes.values() if n["status"] == "UNDER_ATTACK")
        if compromised_count > 0:
            # Deduct health over time based on threat footprint
            game_metrics["global_integrity"] = max(0, game_metrics["global_integrity"] - (compromised_count * 2))
        else:
            # Slow recovery if completely secure
            game_metrics["global_integrity"] = min(100, game_metrics["global_integrity"] + 1)

        # Campaign Wave Events based on time intervals
        if elapsed == 20:
            game_metrics["current_wave"] = 2
            game_metrics["pending_narrative"] = "[ALERT]: Visual scanner shows a minor script injection targetting our Web Server!"
            network_nodes["192.168.1.20"]["status"] = "UNDER_ATTACK"
            network_nodes["192.168.1.20"]["traffic"] = 92
        elif elapsed == 45:
            game_metrics["current_wave"] = 3
            game_metrics["pending_narrative"] = "[CRITICAL]: NullSector has initiated a coordinated assault on your Auth Node and Main DB! Deploy automation!"
            network_nodes["192.168.1.30"]["status"] = "UNDER_ATTACK"
            network_nodes["192.168.1.30"]["traffic"] = 97
            network_nodes["192.168.1.50"]["status"] = "UNDER_ATTACK"
            network_nodes["192.168.1.50"]["traffic"] = 99
        elif elapsed > 50 and elapsed % 25 == 0:
            # Random ongoing skirmishes past wave 3
            target_ip = random.choice(list(network_nodes.keys()))
            network_nodes[target_ip]["status"] = "UNDER_ATTACK"
            network_nodes[target_ip]["traffic"] = random.randint(80, 98)

# --- SCRIPTING ENGINE SANDBOX BRIDGE ---
class CyberAPI:
    @staticmethod
    def get_nodes(): return network_nodes
    @staticmethod
    def get_metrics(): return game_metrics
    @staticmethod
    def mitigate(ip):
        if ip in network_nodes:
            network_nodes[ip]["status"] = "SECURE"
            network_nodes[ip]["traffic"] = random.randint(6, 14)
            return True
        return False

def execute_player_script(script_name):
    script_path = os.path.join(SCRIPTS_DIR, script_name)
    if not os.path.exists(script_path):
        return f"Error: Script '{script_name}' not found."
    try:
        spec = importlib.util.spec_from_file_location("player_script", script_path)
        player_module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(player_module)
        if hasattr(player_module, "main"):
            return player_module.main(CyberAPI)
        return "Error: Script missing 'def main(api):' entry hook."
    except Exception as e:
        return f"Script Crash: {str(e)}"

# --- SOCKET CORE COMMUNICATIONS ---
def handle_java_client(client_socket):
    while True:
        try:
            data = client_socket.recv(4096).decode('utf-8')
            if not data: break

            request = json.loads(data)
            command = request.get("command")
            response = {}

            if command == "GET_NETWORK_STATUS":
                # package nodes, performance indicators, and narrative telemetry together
                response = {
                    "status": "SUCCESS",
                    "nodes": network_nodes,
                    "integrity": game_metrics["global_integrity"],
                    "wave": game_metrics["current_wave"],
                    "clock": game_metrics["system_clock"]
                }
                if game_metrics["pending_narrative"]:
                    response["narrative"] = game_metrics["pending_narrative"]
                    game_metrics["pending_narrative"] = None # Clear message flag once transmitted

            elif command == "DEPLOY_FILTER":
                target_ip = request.get("target_ip")
                if target_ip in network_nodes:
                    network_nodes[target_ip]["status"] = "SECURE"
                    network_nodes[target_ip]["traffic"] = random.randint(5, 12)
                    response = {"status": "SUCCESS", "message": f"Successfully patched firewalls protecting {target_ip}."}

            elif command == "SCAN_SYSTEMS":
                # Package a clean string map of security insights
                scan_report = "\\n=== NET OPS SCAN REPORT ===\\n"
                for ip, details in network_nodes.items():
                    scan_report += f"-> Node: {details['name']} ({ip}) | OS: {details['os']} | Vulnerability Profile: {details['vuln']}\\n"
                response = {"status": "SUCCESS", "message": scan_report}

            elif command == "RUN_SCRIPT":
                script_result = execute_player_script(request.get("script_file"))
                response = {"status": "SUCCESS", "message": f"Script Execution Result: {script_result}"}

            client_socket.sendall((json.dumps(response) + "\n").encode('utf-8'))
        except Exception:
            break
    client_socket.close()

def start_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("127.0.0.1", 9999))
    server.listen(5)
    print("[*] Tactical Defense Engine server active on port 9999...")

    threading.Thread(target=game_master_loop, daemon=True).start()

    while True:
        client_sock, addr = server.accept()
        threading.Thread(target=handle_java_client, args=(client_sock,), daemon=True).start()

if __name__ == "__main__":
    start_server()
