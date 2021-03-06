angular
    .module('isa-mrs-project')
    .factory('employeeService', employeeService);

employeeService.$inject = ['$http'];

function employeeService($http) {
    var service = {
        getEmployee: getEmployee,
        updateEmployee: updateEmployee,
        getSchedule: getSchedule,
        createCook: createCook,
        createWaiter: createWaiter,
        createBartender: createBartender,
        prepareOrderItem: prepareOrderItem,
        finishOrderItem: finishOrderItem,
        getAcceptedItems: getAcceptedItems,
        getEmployees: getEmployees,
        getEmployeesByRestaurant: getEmployeesByRestaurant
    };

    return service;

    function getEmployee(id) {
        return $http.get('api/employees/' + id)
            .then(function(response){
                return response.data;
            });
    };

    function getEmployees() {
        return $http.get('api/employees')
            .then(function(response){
                return response.data;
            });
    };

    function getEmployeesByRestaurant(id) {
        return $http.get('api/employees/r=' + id)
            .then(function(response){
                return response.data;
            });
    };

    function updateEmployee(emp) {
        return $http.put('/api/employee', emp)
            .then(function (response) {
                return response.data;
            });
    };

    function getSchedule() {
        return $http.get('api/employee/schedule')
            .then(function(response) {
                return response.data;
            });
    };

    function createBartender(bartender) {
        return $http.post('api/bartender', bartender)
            .then(function(response) {
                return response.data;
            });
    };

    function createWaiter(waiter) {
        return $http.post('api/waiter', waiter)
            .then(function(response) {
                return response.data;
            });
    };

    function createCook(cook) {
        return $http.post('api/cook', cook)
            .then(function(response) {
                return response.data;
            });
    };

    function prepareOrderItem(itemId, empId){
        return $http.post('/api/orderItems/i=' + itemId + '/e=' + empId)
            .success(function(response) {
                return response.data;
            })
            .error(function(response) {
                return response.data;
            });
    }

    function getAcceptedItems(empId){
        return $http.get('api/orderItems/itemsForPrepare/e=' + empId)
            .then(function (response) {
                return response.data;
            });
    }

    function finishOrderItem(itemId) {
        return $http.put('/api/orderItems/i=' + itemId)
            .then(function (response) {
                return response.data;
            });
    }
}
