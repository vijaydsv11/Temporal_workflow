FROM python:3.11-slim

WORKDIR /app

COPY <<'PY' /app/app.py
from http.server import BaseHTTPRequestHandler, HTTPServer
import json

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(length) if length else b''
        print(f"Received batch payload: {body.decode('utf-8', errors='replace')[:500]}")
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({"status": "processed"}).encode('utf-8'))

    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({"status": "ok"}).encode('utf-8'))

    def log_message(self, format, *args):
        return

server = HTTPServer(('0.0.0.0', 8081), Handler)
print('Batch service listening on 0.0.0.0:8081')
server.serve_forever()
PY

EXPOSE 8081

CMD ["python", "/app/app.py"]
