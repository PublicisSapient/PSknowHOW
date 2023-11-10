#!/usr/bin/env python
import sys

def select_ip():
    ip_addresses = ["192.168.1.1", "10.0.0.1", "172.16.0.1"]  # Add your list of IP addresses

    print("Select an IP address:")
    for i, ip in enumerate(ip_addresses, start=1):
        print(f"{i}. {ip}")

    try:
        choice = int(input("Enter the number of the selected IP address: "))
        selected_ip = ip_addresses[choice - 1]
        print(f"You selected: {selected_ip}")
        return selected_ip
    except (ValueError, IndexError):
        print("Invalid choice. Please enter a valid number.")
        sys.exit(1)

if __name__ == "__main__":
    selected_ip = select_ip()
    # You can use the selected_ip in your GitHub Actions workflow
    print(f"Selected IP address: {selected_ip}")
