#!/usr/bin/env bash
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/B23DTCN112_DOMINHVU/source"
OUT="$HOME/.cache/ltm-quanlythuoc"
CP="$OUT/classes:$ROOT/lib/mysql-connector-j-9.3.0.jar"

mysql_ready() {
    (exec 3<>/dev/tcp/127.0.0.1/3306) 2>/dev/null
}

case "$1" in
caidat)
    sudo apt-get update
    sudo apt-get install -y mariadb-server openjdk-21-jdk
    sudo systemctl enable --now mariadb
    sudo mysql -e "ALTER USER root@localhost IDENTIFIED VIA mysql_native_password USING PASSWORD(''); FLUSH PRIVILEGES;"
    echo "Cài xong. Giờ chạy: ./chay_bai.sh"
    exit 0
    ;;
client)
    exec java -cp "$CP" bt3.client.MedicineClient
    ;;
reset)
    mysql -h127.0.0.1 -uroot -e "DROP DATABASE IF EXISTS quanlythuoc" 2>/dev/null \
        || docker exec ltm-mysql mysql -uroot -e "DROP DATABASE IF EXISTS quanlythuoc"
    echo "Đã xóa database, chạy lại ./chay_bai.sh để có dữ liệu mẫu."
    exit 0
    ;;
esac

echo "1) Kiểm tra MySQL..."
if ! mysql_ready; then
    if command -v docker >/dev/null; then
        docker start ltm-mysql >/dev/null 2>&1 || docker run -d --name ltm-mysql -p 3306:3306 \
            -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e TZ=Asia/Ho_Chi_Minh mysql:8.4 >/dev/null
        until docker exec ltm-mysql mysql -uroot -e "SELECT 1" >/dev/null 2>&1; do sleep 2; done
    else
        sudo systemctl start mariadb 2>/dev/null || true
        mysql_ready || { echo "Chưa có MySQL. Chạy một lần: ./chay_bai.sh caidat"; exit 1; }
    fi
fi

echo "2) Biên dịch..."
rm -rf "$OUT/classes"
javac -encoding UTF-8 -d "$OUT/classes" $(find "$SRC/src/main/java" -name '*.java')
cp -r "$SRC/src/main/resources/." "$OUT/classes/"

echo "3) Mở Client trong 3 giây (mở thêm Client: ./chay_bai.sh client)"
(sleep 3 && java -cp "$CP" bt3.client.MedicineClient) &

echo "4) Chạy Server (Ctrl+C để tắt):"
exec java -cp "$CP" bt3.server.MedicineServer
