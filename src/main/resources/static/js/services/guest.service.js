angular
    .module('isa-mrs-project')
    .factory('guestService', guestService);

guestService.$inject = ['$http'];

function guestService($http) {
    var service = {
        getGuest: getGuest,
        updateGuest: updateGuest,
        getRequests: getRequests,
        getFriends: getFriends,
        accept: accept,
        reject: reject,
        isMy: isMy,
        addFriend: addFriend,
        removeFriend: removeFriend,
        isFriend: isFriend,
        getSearchResult: getSearchResult,
        getVisits: getVisits,
        getReservations: getReservations,
        getInvitations: getInvitations,
        acceptInvite: acceptInvite,
        declineInvite: declineInvite,
        resendToken: resendToken,
        isLoggedIn: isLoggedIn,
        cancelMealOrder: cancelMealOrder,
        isRequestSent: isRequestSent
    };

    return service;

    function getGuest(id) {
        return $http.get('api/guest/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function updateGuest(user) {
        return $http.put('api/user/update', user)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getRequests() {
        return $http.get('api/guest/requests')
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getFriends(id) {
        return $http.get('api/guest/friends/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function accept(id) {
        return $http.put('api/guest/accept-friend/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function reject(id) {
        return $http.put('api/guest/reject-friend/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function acceptInvite(id) {
        return $http.put('api/guest/accept-invite/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function declineInvite(id) {
        return $http.put('api/guest/decline-invite/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function isMy(id) {
        return $http.get('api/guest/admin/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function addFriend(id) {
        return $http.post('api/guest/add-friend/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function removeFriend(id) {
        return $http.delete('api/guest/remove-friend/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function isFriend(id) {
        return $http.get('api/guest/friend/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getSearchResult(query) {
        return $http.get('api/guest/users?query=' + query)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getReservations(id) {
        return $http.get('api/guest/reservations/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getVisits(id) {
        return $http.get('api/guest/visits/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function getInvitations(id) {
        return $http.get('api/guest/invitations/' + id)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function resendToken(email) {
        return $http.put('api/registration-token-resend', email)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function isLoggedIn() {
        return $http.get('api/guest/logged')
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function cancelMealOrder(orderId) {
        return $http.delete('api/order/' + orderId)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
    
    function isRequestSent(guestId) {
        return $http.get('api/guest/is-request-sent/' + guestId)
            .success(function(data) {
                return data;
            })
            .error(function(data) {
                return data;
            });
    };
}