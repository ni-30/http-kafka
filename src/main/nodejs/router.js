const dummyProcessor = require('./processors/dummyProcessor')

module.exports = {
    route: function(metadata, data, onComplete) {
        routePath = metadata.routePath;
        routePathArr = routePath.split('/');
        if (routePathArr.length == 0 || routePathArr[routePathArr.length - 1] == '') {
            onComplete('bad-route-path');
            return;
        }

        path = '';
        for(var i = 0; i < routePathArr.length - 1; i++) {
            path = routePathArr[i] + "/";
        }
        
        processor = this.paths[path];
        if(processor == undefined) {
            onComplete('bad-route-path');
            return;
        }
        
        try {
            processor(metadata, data);
        } finally {
            onComplete('ok');
        }
    },

    paths : {
        "path/to/dummyProcessor/" : dummyProcessor.process
    }
};