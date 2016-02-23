console.log("sApp.js loaded");
var app, dependencies;
dependencies = ["ui.bootstrap"];
app = angular.module("sividApp", dependencies);
app.controller("mainCtrl", ["$scope", "$http", "$uibModal", "modalFac", "svcFac",
function($scope, $http, $uibModal, modalFac, svcFac) {
  $scope.items = [];
  $scope.refreshList = function() {
    $http.get("/persons/").then(function(response) {
      console.log("refreshList fn response ", response);
      $scope.items = response.data[0];
      console.log("modalFac.instance", modalFac.instance);
    }, function(status) {
      console.log(status);
    });
  } // end $scope.refreshList()
  $scope.modifyPerson = function(id) {
    modalFac.instance = $uibModal.open({
      animation: true,
      templateUrl: 'myModalContent.html',
      controller: "modalCtrl"
    });
    svcFac.person_mongo_id = id;
    modalFac.instance.result.then(function (selectedItem) {
//      console.log("modal closed");
        $scope.refreshList();
    }, function () {
//      console.log('Modal dismissed at: ' + new Date());
    });
  } // end $scope.modifyPerson()
  $scope.removePerson = function(id) {
    $http.delete("/person/" + id).then(function(response) {
      console.log(id, "removed");
      $scope.refreshList();
    }, function(status) {
      console.log(status);
    });
  };
}]);

app.controller("modalCtrl", ["modalFac", "$scope", "svcFac", "$http", function(modalFac, $scope, svcFac, $http) {
  $scope.m = {};
  $scope.personModifySubmit = function() {
    if (!svcFac.isValidUser($scope.m.age, $scope.m.fName, $scope.m.lName)) {
      return;
    }
    var data = {
      age: Number.parseInt($scope.m.age),
      firstName: $scope.m.fName,
      lastName: $scope.m.lName,
      isActive: true
    };
    console.log("going to submit", svcFac.person_mongo_id, data);
    $http.put("person/" + svcFac.person_mongo_id, data).then(function(response) {
      console.log("modified user", svcFac.person_mongo_id);
    }, function(status) {
      console.log(status);
    });

    modalFac.instance.close();
  };
  $scope.modifyPersonCancel = function() {
    modalFac.instance.dismiss("cancel");
  }
}]);


app.controller("addPersonCtrl", ["$scope", "$http", "svcFac", function($scope, $http, svcFac) {
  $scope.msg = "yo";
  $scope.personSubmit = function() {
    if (!svcFac.isValidUser($scope.age, $scope.fName, $scope.lName)) {
      return;
    }
    var data = {
      age: Number.parseInt($scope.age),
      firstName: $scope.fName,
      lastName: $scope.lName,
      isActive: true
    }
    $http.post("/person/", data).then(function(response) {
      console.log(response);
      delete $scope.age;
      delete $scope.fName;
      delete $scope.lName;
    }, function(status) {
      console.log(status);
    })
  } // end $scope.personSubmit()
}]);
app.factory("modalFac", function() {
  var obj = {
    instance: null,
  };
  return obj;
});
app.factory("svcFac", function() {
  return {
    isValidUser: function(age, fName, lName) {
      if (Number.isInteger(age)) {
        console.log("age needs to be an Integer, but it is ", typeof age); // pretty alerts can wait
        return false;
      }
      if (typeof fName !== "string" || typeof lName !== "string") {
        console.log("names need to be a string");
        return false;
      }
      return true;
    },
    person_mongo_id: 0
  };
});