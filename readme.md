mvn clean package
docker compose up -d --build


sudo lsof -i :8080
sudo kill -9 12345


docker exec -it backend-db-1 psql -U postgres -d quizroom
docker exec -it backend-app-1 env | grep CLIENT


docker compose down