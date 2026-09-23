#!/usr/bin/env python3
"""Wait for the exact Git revision to become healthy, not merely the old app."""
import json
import sys
import time
import urllib.error
import urllib.request

expected = sys.argv[1]
url = "http://127.0.0.1:8081/student-feedback/health"
last_error = "No response"
for attempt in range(30):
    try:
        with urllib.request.urlopen(url, timeout=3) as response:
            result = json.load(response)
            if (response.status == 200 and result.get("status") == "UP"
                    and result.get("application") == "student-feedback"
                    and result.get("revision", "local") == expected):
                print(json.dumps(result))
                sys.exit(0)
            last_error = "Unexpected health response: " + json.dumps(result)
    except (OSError, ValueError) as exc:
        last_error = str(exc)
    time.sleep(2)
print("Health check failed: " + last_error, file=sys.stderr)
sys.exit(1)
