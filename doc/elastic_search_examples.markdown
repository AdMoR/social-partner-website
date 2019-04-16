# Running the elastic search example for event retrieval

#### 1. Install docker 
https://www.docker.com/

#### 2. Launch the elastic search container
sudo docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.0.0


#### 3. Launch a python shell

```python
import requests

endpoint = "http://localhost:9200"
requests.put(endpoint + "/events/_doc/1", json.dumps({"event_id": 1, "title": "Super party"}), 
             headers={"content-type": 'application/json; charset=utf8'})
requests.put(endpoint + "/events/_doc/2", json.dumps({"event_id": 2, "title": "Super event"}), 
             headers={"content-type": 'application/json; charset=utf8'})

requests.get(endpoint + "/events/_search?q=title:super")
```

You should get a 200 meaning that you successfully retrieved events with "super" in the title