angular
    .module('isa-mrs-project')
    .factory('bartenderService', bartenderService);

bartenderService.$inject = ['$http'];

function bartenderService($http){
    var service = {
        getBartender: getBartender,
        updateBartender: updateBartender
    };

    return service;

    function getBartender(id){
        return $http.get('api/bartenders/' + id )
            .then(function(response){
                return response.data;
            });
    };

    function updateBartender(bartender){
        return $http.put('/api/bartender', bartender)
            .then(function (response) {
                return response.data;
            })
    };


}