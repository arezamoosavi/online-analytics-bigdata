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

kafkacat:
	docker-compose up -d kafkacat
	docker-compose exec kafkacat kafkacat -b kafka:9092 -C -o beginning -t click_topic | jq


pg:
	docker-compose up -d pgworker

superset:
	docker-compose up -d superset

druid:
	docker-compose up -d zookeeper
	sleep 3
	docker-compose up -d druid

rm-druid:
	docker-compose stop druid
	echo y | docker-compose rm druid


# create_table:
#     create tasble click_events
#     (
#     	anon_id text,
#     	query text,
#     	query_time text,
#     	item_rank text,
#     	click_url text
#     );


