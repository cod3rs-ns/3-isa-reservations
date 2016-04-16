angular
    .module('isa-mrs-project')
    .factory('restaurantService', restaurantService);

restaurantService.$inject = ['$http'];

function restaurantService($http){
    var service = {
        getRestaurants: getRestaurants,
        getRestaurant: getRestaurant,
        updateRestaurant: updateRestaurant,
        deleteRestaurant: deleteRestaurant,
        createRestaurant: createRestaurant
    };

    return service;

    function getRestaurants(){
        return $http.get('api/restaurants')
        .then(function(response) {
            return response.data;
        });
    };

    function getRestaurant(id){
        return $http.get('api/restaurants/' + id)
        .then(function(response){
            return response.data;
        });
    };

    function updateRestaurant(restaurant, id){
        return $http.put('/api/restaurants/' + id, restaurant)
        .then(function (response) {
            return response.data;
        })
    };

    function deleteRestaurant(id){

    };

    function createRestaurant(restaurant, id){
        console.log("create");
        return $http.post('/api/restaurants/' + id, restaurant)
        .then(function (response) {
            return response.data;
        })
    };

}