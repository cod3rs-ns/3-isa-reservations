angular
    .module('isa-mrs-project')
    .controller('TablesController', TablesController);

TablesController.$inject = ['tableService', 'restaurantManagerService', 'regionService', '$mdDialog', '$mdToast'];

function TablesController(tableService, restaurantManagerService, regionService, $mdDialog, $mdToast) {
    var tablesVm = this;
    // switch of edit or no-edit state
    tablesVm.enabledEdit = false;
    // table containers
    tablesVm.tables = [];
    tablesVm.deletedTables = [];
    tablesVm.backup = [];

    // objects of interaction (canvas and all its object separated)
    tablesVm.interactObject = null;
    tablesVm.interactCanvas = null;

    // processing state changes
    tablesVm.saveChanges = saveChanges;
    tablesVm.cancelChanges = cancelChanges;

    // saves current layout, used for backup
    tablesVm.saveCurrentState = saveCurrentState;

    // calculates number of people for table
    tablesVm.tableSizeHeuristic = tableSizeHeuristic;

    // enable interaction with elements
    tablesVm.startInteraction = startInteraction;

    // toogle editState attribute true/false and do appropriate actions
    tablesVm.switchCanvasState = switchCanvasState;

    // returns table with provided ID
    tablesVm.findTable = findTable;

    // feedback to user
    tablesVm.showInfo = showInfo;
    tablesVm.showToast = showToast

    tablesVm.tableColors = ['default', 'first-color', 'second-color' , 'third-color',
                            'fourth-color', 'fifth-color', 'sixth-color', 'seventh-color',
                            'eighth-color', 'ninth-color'];
    tablesVm.hexColors = [  '#283593', '#00695C', '#AD1457', '#CDDC39','#0277BD',
                            '#5D4037', '#607D8B', '#E91E63', '#43A047', '#76FF03'];
    tablesVm.regionCount = 1;
    tablesVm.regions = [];
    tablesVm.regionEditFields = [];
    tablesVm.addNewRegion = addNewRegion;
    tablesVm.backupRegion = null;
    tablesVm.saveRegion = saveRegion;
    tablesVm.deleteRegion = deleteRegion;
    tablesVm.getNextRestaurantTableNo = getNextRestaurantTableNo;
    tablesVm.tablesInRestaurantNumbers = [];
    activate();

    function activate() {
        restaurantManagerService.getLoggedRestaurantManager()
            .then(function(data) {
                tablesVm.rmanager = data;
                getTablesByRestaurant().then(function() {
                    // after success action
                });


                getRegionsByRestaurant();
                tablesVm.regionCount = tablesVm.regions.length;


                for (var i = 0; i < tablesVm.regions.length; i++) {
                    tablesVm.regionEditFields[i] = false;
                }
            });

    };

    function getTablesByRestaurant() {
        // TODO currently locked on 2
        // find way to access to logged user
        return tableService.getTablesByRestaurant(tablesVm.rmanager.restaurant.restaurantId)
            .then(function(data) {
                tablesVm.tables = data;

                for (var i = 0; i < tablesVm.tables.length; i++) {
                    tablesVm.tablesInRestaurantNumbers.push(tablesVm.tables[i].tableInRestaurantNo);
                };
                tablesVm.tablesInRestaurantNumbers.sort();
                console.log(tablesVm.tablesInRestaurantNumbers);
                return tablesVm.tables;
            });
    };

    function getRegionsByRestaurant() {
        // TODO currently locked on 2
        // find way to access to logged user
        return regionService.getRegionsByRestaurant(tablesVm.rmanager.restaurant.restaurantId)
            .then(function(data) {
                tablesVm.regions = data;
                tablesVm.regionCount = tablesVm.regions.length;
            })
    }

    function showInfo(ev){
        $mdDialog.show(
            $mdDialog.alert()
                .parent(angular.element(document.querySelector('#upper')))
                .clickOutsideToClose(true)
                .title('Uputstvo za korišćenje')
                .textContent(
                    'Za dodavanje novog stola potrebno je zadržati klik na praznoj površini dok se ne pojavi sto '
                +   'Za uklanjanje postojećeg stola potrebno je uraditi dupli klik na njegovoj površini. '
                +   'Za promenu regiona stola potrebno je kliknuti na sto i boja će mu se promeniti. '
                +   'Promenom veličine stola menja se i broj ljudi koji mogu da sednu za njega. '
                +   'Promena veličine omogućena je u levoj i donjoj ivici stola, prevlačenjem miša do željene pozicije. '
                +   'Promena pozicije stola moguća je prostim prevlačenjem stola na drugu poziciju. '
                +   'Sve promene se čuvaju tek nakon klika na dugme Sačuvaj. Ukoliko se klikne na dugme Poništi, sve '
                +   'promene će biti poništene i raspored će biti vraćen u pređašnje stanje.')
                .ariaLabel('Uputstvo za korišćenje')
                .ok('OK')
                .targetEvent(ev)
            );
    };

    function showToast(toast_message) {
        $mdToast.show({
            hideDelay : 3000,
            position  : 'bottom left',
            template  : '<md-toast><strong>' + toast_message + '<strong> </md-toast>'
        });
    };

    function saveChanges() {
        // TODO Region currently locked onto 1
        for (var i = 0; i < tablesVm.tables.length; i++) {
            // create new tables
            if (tablesVm.tables[i].hasOwnProperty('isNew')) {
                tableService.createTable(angular.toJson(tablesVm.tables[i]), tablesVm.tables[i].region.regionId);
            }else{ // update existing tables
                tableService.updateTable(angular.toJson(tablesVm.tables[i]), tablesVm.tables[i].region.regionId);
            };
        };

        // remove deleted tables from database
        for (var i = 0; i < tablesVm.deletedTables.length; i++) {
            tableService.deleteTable(tablesVm.deletedTables[i].tableId);
        };
        // reset list for next time
        tablesVm.deletedTables = [];
        tablesVm.switchCanvasState();
        tablesVm.showToast('Raspored stolova je sačuvan.')
    };

    function cancelChanges() {
        tablesVm.tables = tablesVm.backup;
        tablesVm.backup = [];
        tablesVm.deletedTables = [];
        tablesVm.switchCanvasState();
        tablesVm.showToast('Sve promene su poništene.')
    };

    function saveCurrentState(){
        tablesVm.backup = angular.copy(tablesVm.tables);
        tablesVm.deletedTables = [];
    };

    function tableSizeHeuristic(x, y){
        // TODO This is example of function, should be way smarter
        return Math.ceil((x*y)/25.0);
    };

    function startInteraction(){
        // Define how should tables behave
        var container = document.getElementById('canvas');

        tablesVm.interactObject = interact('.restaurant-table')
            .origin({
                x: container.offsetLeft,
                y: container.offsetTop
            })
            .draggable({
                // enable inertial throwing
                inertia: false,
                // keep the element within the area of it's parent
                restrict: {
                    restriction: 'parent',
                    endOnly: true,
                    elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
                },
                // enable autoScroll
                autoScroll: false,
                // call this function on every dragmove event
                onmove: dragMoveListener,
                // call this function on every dragend event
                onend: null
            })
            .resizable({
                preserveAspectRatio: false,
                // define on which edges should table be resizable
                edges: { left: true, right: true, bottom: true, top: true }
            })
            .on('resizemove', resizeListener)
            .on('tap', colorSwitchListener)
            .on('doubletap', removeListener);

            tablesVm.interactCanvas = interact('.restaurant-canvas')
                .on('hold', addTableListener);
    };

    function getNextRestaurantTableNo(){
        var nextTableNo = -1;
        for (var i = 0; i < tablesVm.tablesInRestaurantNumbers.length; i++) {
            if(tablesVm.tablesInRestaurantNumbers[i] != (i+1)){
                nextTableNo = i+1;
                break;
            };
        }
        if(nextTableNo == -1){
            nextTableNo = tablesVm.tablesInRestaurantNumbers.length + 1;
        };


        return nextTableNo;
    }

    function addTableListener(event) {
        tablesVm.newTable = {
            tableId: null,
            datax: event.offsetX/event.target.offsetWidth*100,
            datay: event.offsetY/event.target.offsetHeight*100,
            width: 8,
            height: 8,
            positions: 2,
            tempId: 10000 + tablesVm.tables.length,
            isNew: true,
            region_color: 'green',
            region: tablesVm.regions[0],
            tableInRestaurantNo: tablesVm.getNextRestaurantTableNo()
        };

        tablesVm.tables.push(tablesVm.newTable);
        tablesVm.tablesInRestaurantNumbers.push(tablesVm.newTable.tableInRestaurantNo);
        tablesVm.tablesInRestaurantNumbers.sort();
        console.log(tablesVm.tablesInRestaurantNumbers);
        var parent = document.getElementById('canvas');
        var test =  document.createElement("p");
        parent.appendChild(test);
        parent.removeChild(test);
    };

    function colorSwitchListener(event) {
        var to_disable = null;
        var index = -1;
        var current = null;
        var stylename = null;
        for (var j = 0; j < tablesVm.tableColors.length; j++) {
            stylename = tablesVm.tableColors[j];
            index = index + 1;
            for (var i = 0; i < event.currentTarget.classList.length; i++) {
                current = event.currentTarget.classList[i];
                if (stylename == current){
                    to_disable = current;
                    break;
                }
            }
            if(to_disable != null){
                break;
            }
        }
        event.currentTarget.classList.toggle(to_disable);
        var next = (index+1)%(tablesVm.regionCount);
        event.currentTarget.classList.toggle(tablesVm.tableColors[next]);
        var currentTable = tablesVm.findTable(event.currentTarget.getAttribute('id'));
        currentTable.region = tablesVm.regions[next];
        event.preventDefault();
    };

    // Function that saves current positon of the element after dragging
    function dragMoveListener(event) {
        var target = event.target;
        // parent canvas size
        var parentWidth = target.parentElement.offsetWidth;
        var parentHeight = target.parentElement.offsetHeight;

        var oldPx = parseFloat(target.getAttribute('data-x')) || 0;
        var oldPy = parseFloat(target.getAttribute('data-y')) || 0;
        var newX = (event.dx / parentWidth) * 100 + oldPx;
        var newY = (event.dy / parentHeight) * 100 + oldPy;

        var width = parseFloat(target.getAttribute('width'));
        var height = parseFloat(target.getAttribute('height'));
        // neccesary for automatic update :) lost 30 minutes because of this
        target.style = 'top:' + newY + '%;left:' + newX + '%;height:' + height +'%;width:'+ width + '%;';
        // update the posiion attributes
        target.setAttribute('data-x', newX);
        target.setAttribute('data-y', newY);
        var currentTable = tablesVm.findTable(target.getAttribute('id'));
        currentTable.datax = newX;
        currentTable.datay = newY;
    };

    // Function that saves current size of element after resizing
    function resizeListener(event) {
        var target = event.target;
        var x = (parseFloat(target.getAttribute('data-x')) || 0);
        var y = (parseFloat(target.getAttribute('data-y')) || 0);

        // parent canvas size
        var parentWidth = target.parentElement.offsetWidth;
        var parentHeight = target.parentElement.offsetHeight;

        var currentTable = tablesVm.findTable(target.getAttribute('id'));

        var newWidth = (event.rect.width / parentWidth) * 100;
        var newHeight = (event.rect.width / parentWidth) * 100;

        // update the element's style, neccesary for automatic view update
        target.style.width  = newWidth + '%';
        target.style.height = newHeight + '%';

        // update model
        currentTable.height = newHeight;
        currentTable.width =  newWidth;
        currentTable.positions = tablesVm.tableSizeHeuristic(newWidth, newHeight);
        console.log(currentTable);
        console.log(tablesVm.tableSizeHeuristic(newWidth, newHeight))
        console.log(currentTable.positions);
        // translate when resizing from top or left edges
        x += (event.deltaRect.left / parentWidth) * 100 ;
        y += (event.deltaRect.top / parentWidth) *100;

        target.setAttribute('data-x', x);
        target.setAttribute('data-y', y);
        // neccesary for automatic update :)
        target.style = 'top:' + y + '%;left:' + x + '%;height:' + newHeight +'%;width:'+ newWidth + '%;';
        currentTable.datax = x;
        currentTable.datay = y;

        // TODO tableSizeHeuristic
        //target.textContent = 'n:' + 'br'
    };

    function removeListener(event) {
        var target = event.target;
        console.log(target);
        var id = parseInt(target.getAttribute('id'));
        var toRemove = -1;
        for (var i = 0; i < tablesVm.tables.length; i++) {
            if(tablesVm.tables[i].hasOwnProperty('isNew')){
                console.log('new');
                if(tablesVm.tables[i].tempId == id){
                    toRemove = i;
                    break;
                }
            }else{
                if(tablesVm.tables[i].tableId == id){
                    toRemove = i;
                    break;
                };
            };

        };
        console.log('toRemove' + toRemove);
        // add to deleted tables only if table already exists in database
        if(tablesVm.tables[toRemove].hasOwnProperty('isNew') === false){
            tablesVm.deletedTables.push(angular.copy(tablesVm.tables[toRemove]));
        };

        var parent = document.getElementById("canvas");
        var child = document.getElementById(id);
        parent.removeChild(child);

        tablesVm.tables.splice(toRemove, 1);
    };

    // this is used on whole window when draggin or resizing
    // TODO maybe this should be restricted only to canvas
    window.dragMoveListener = dragMoveListener;
    window.resizeListener = resizeListener;
    window.colorSwitchListener = colorSwitchListener;
    window.removeListener = removeListener;
    window.addTableListener = addTableListener;
    //interact.maxInteractions(Infinity);

    function switchCanvasState() {
        tablesVm.enabledEdit = !tablesVm.enabledEdit;
        if(tablesVm.enabledEdit) {
            tablesVm.startInteraction();
            tablesVm.saveCurrentState();
            document.getElementById('canvas').classList.toggle('edit-bg');
        }else {
            tablesVm.interactObject.unset();
            tablesVm.interactCanvas.unset();
            document.getElementById('canvas').classList.toggle('edit-bg');
        }

    }

    function findTable(table_id) {
        for (var i = 0; i < tablesVm.tables.length; i++) {
            if ( tablesVm.tables[i].hasOwnProperty('isNew')) {
                // use .tempId
                if(tablesVm.tables[i].tempId == table_id) {
                    return tablesVm.tables[i];
                };
            }else {
                // use standard .tableId
                if(tablesVm.tables[i].tableId == table_id) {
                    return tablesVm.tables[i];
                };
            };
        };
        return null;
    };

    function addNewRegion() {
        var nextFreeNo = -1;
        for (var i = 0; i < tablesVm.regions.length; i++) {
            if(tablesVm.regions[i].regionNo != (i+1) ){
                nextFreeNo = i+1;
                break;
            };
        };

        if(nextFreeNo == -1) {
            if(tablesVm.regions.length < 10){
                nextFreeNo = tablesVm.regions.length + 1;
            };
        };

        if(nextFreeNo == -1){
            // TODO Show alert
            alert("No free region slots");
            return;
        }
        var newRegion = {
            regionId: null,
            name: "novi_region",
            regionNo: nextFreeNo,
            color: tablesVm.hexColors[nextFreeNo-1]
        };
        tablesVm.regions.push(newRegion);
        tablesVm.regionCount = tablesVm.regionCount + 1;
        regionService.createRegion(newRegion, tablesVm.rmanager.restaurant.restaurantId).then(
            function(data){
                console.log(data);
            }
        );
    };

    function saveRegion(idx) {
        regionService.updateRegion(tablesVm.regions[idx], tablesVm.rmanager.restaurant.restaurantId)
            .then(function(data){
                console.log(data);

            });
        tablesVm.regionEditFields[idx] = false;
    };

    function deleteRegion() {
        tablesVm.regionEditFields[idx] = false;
        //tablesVm.regionEditFields.splice(idx, 1);

    }
}
