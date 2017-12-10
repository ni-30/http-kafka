const http = require('http');
const querystring = require('querystring');
const config = require('../resources/config.json');

class publisher {
    constructor() {
        this.host = config.http.publisher.host;
        this.port = config.http.publisher.port;
        this.path = config.http.publisher.path;
    }
    
    publish(metadata, data, callback) {
        let options = {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json',
              'X-RouterPath': metadata.routerPath,
              'X-Topic': metadata.topic
            }
        };
        options['hostname'] = host;
        options['port'] = port;
        options['path'] = path;

        let req = http.request(options, (res) => {
            if(res.statusCode == 200) {
                let data = {
                    topic: res.headers['X-Topic'],
                    partition: res.headers['X-Partition'],
                    offset: res.headers['X-Offset'],
                    producerTimestamp: res.headers['X-Producer-Timestamp']
                };
                callback(null, data);
            } else {
                callback(new Error('status is ' + res.statusCode), null);
            }
        });
        req.on('error', (e) => {
            callback(e, null);
        });
        req.write(querystring.stringify(data));
        req.end();
    }
}

module.exports = publisher;