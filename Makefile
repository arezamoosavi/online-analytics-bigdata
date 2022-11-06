.PHONY: pull up logs ps down pg kafka superset druid

clear:
	echo y | docker image prune --filter="dangling=true"

pull:
	docker-compose pull

up:
	docker-compose up -d

logs:
	docker-compose logs -f

ps:
	docker-compose ps

down:
	docker-compose down -v

kafka:
	docker-compose up -d zookeeper
	sleep 3
	docker-compose up -d kafka

pg:
	docker-compose up -d pgworker

superset:
	docker-compose up -d superset

druid:
	docker-compose up -d zookeeper
	sleep 3
	docker-compose up -d druid
