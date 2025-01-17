var baseUrl =                           $('#baseUrl').html();
var attachReceiptLabel =                $('#attachReceipt').html();
var newReportLabel =                    $('#newReportString').html();
var receiptText =                       $('#receipts').html();
var newReportString =                   $('#newReportString').html();

var createExpenseReportExpenseUrl =     baseUrl +   "reports/reportId/expenses";
var eligibleChargesUrl =                baseUrl +   'reports/eligible-charges/';
var createErUrl =                       baseUrl +   'reports';
var expenseReportUrl =                  baseUrl +   'reports/expenseReportId';
var expenseUrl =                        baseUrl +   'reports/expenses/expenseId';
var expenseReportExpensesUrl =          baseUrl +   'reports/reportId/expenses';
var reportPurposeUrl =                  baseUrl +   'reports/reportId/purpose';
var openReportsUrl =                    baseUrl +   "reports/open-reports";
var receiptsUrl =                       baseUrl +   'reports/receipts';
var expenseReceiptImagePreviewUrl =     baseUrl +   'reports/receipts/receiptId';

var previewedNodes = [];
var currentlyDraggedExpenseId = null;
var movingChargeToExpenses = false;
var movingExpensesToCharges = false;
var loaderImageSrc = null;
var dropzoneNode = null;
var currentFocusedExpenseElement = null;
var isDragging = false;
var currentExpense = null;
var purposeReport = null;
var expenseReportMenu = null;
var ecsNode = null;
var currentEligibleCharge = null;
var expenseReport = null;
var expensesPanel = null;
var expensesNode = null;
var attachReceiptImageSrc = null;
var currentExpenseReport = null;
var currentExpenseToRevert = null;
var previewNode = null;

var report = { purpose:'' , expenses : '' , id: 0 };

function loadExpenseReportExpensesLocally(expenseReportId, purpose, results, cb) {
    console.log('expenseReportId: ' + expenseReportId + '; purpose: ' + purpose + '; results: ' + results.length);
    // save the report for efficient re-use later
    report.id = expenseReportId;
    report.expenses = results;
    report.purpose = purpose;

    currentExpenseReport = expenseReportId;
    expensesNode.html("");
    purposeReport.html(purpose);
    var p = 0;
    for (var i = 0; i < results.length; i++) {
        var expense = results[i];
        p += expense.amount;
        drawExpense(expensesNode, expense);
    }

    $('#expensesBalance').html(renderPrice(p));

    refreshMenus(expenseReportId);

    if (cb != null) cb();

}

function loadExpenseReportExpenses(expenseReportId) {
    var purpose = null;
    var results = [];
    var urlForReport = expenseReportUrl.replace('expenseReportId',''+expenseReportId); // '/reports/' + expenseReportId;
    var urlForExpenses = expenseReportExpensesUrl.replace('reportId', '' + expenseReportId);
    $.get(urlForReport, function(result) {
        purpose = (result.purpose);
        $.get(urlForExpenses, function(rs) {
            results = rs;
            loadExpenseReportExpensesLocally(expenseReportId, purpose, results);
        }, 'json');

    });
}

function drawEligibleCharge(ecsNode, ec) {
    var ecId = 'ec' + ec.id;
    $(ecsNode).append("<div id='" + ecId + "' class = 'ec'> <span class ='merchant'> " +
            ec.merchant + "</span>" + "<span class ='amount'>" +
            renderPrice(ec.amount) + "</span>" +
            " <span class='category'>" + ec.category + "</span> <span class ='rowSpacer'></span> " +
            "</div>");

    $('#' + ecId).draggable({
        zIndex : 500,
        revert : 'invalid', // ie, itll revert if its not dropped
        start : function () {
            console.log('moving charges to expenses == true');
            movingChargeToExpenses = true;
            currentEligibleCharge = ec;
        },
        stop : function () {
            movingChargeToExpenses = false;
            console.log('moving charges to expenses == false');
        },
        drag : function () {
        }
    });
}

function fileDragging(isIt) {

    isDragging = isIt;

    if (!isIt) {
        console.log("dragging is false");
        dropzoneNode.hide();
    }
    if (isIt) {

        // redraw the box
        var cfe = $(currentFocusedExpenseElement);
        var h = cfe.outerHeight();
        var w = cfe.outerWidth();
        var x = cfe.position().left;
        var y = cfe.position().top;

        dropzoneNode.css({backgroundImage  :'url( ' + attachReceiptImageSrc + ')'});
        dropzoneNode.css({ left :x, top :y, width:w, height:h });
        dropzoneNode.show()

    }

}

function expenseNodeId(ecId) {
    return 'expense' + ecId;
}

function resetExpenseReport(cb) {
    currentExpenseReport = null;
    expensesNode.html("");
    refreshMenus();
    $('#expensesBalance').html(renderPrice(0));
    purposeReport.html(newReportLabel);
    if (cb) cb();
}


function deleteExpenseReport(expenseReportId) {
    var expenseDeletionUrl = expenseReportUrl.replace('expenseReportId', expenseReportId);
    $.ajax({
        type : 'POST',
        url :  expenseDeletionUrl,
        cache : false,
        dataType : 'json',
        data : {
            '_method' :'DELETE',
            expenseReportId:   expenseReportId
        },
        success : function(result) {
            renderEligibleCharges(function() {
                resetExpenseReport()
            });
        },
        error : function(e) {
            console.log('error addEligibleChargeToReport ' + JSON.stringify(e))
        }
    });
}


function restoreExpenseToEligibleCharges(expenseId) {
    //var expenseDeletionUrl = '/' + expenseId;
    $.ajax({
        type : 'POST',
        url : expenseUrl.replace('expenseId', expenseId + ''),
        cache : false,
        dataType : 'json',
        data : {
            '_method' :'DELETE',
            expenseId:   expenseId
        },
        success : function(result) {
            renderEligibleCharges(function() {
                loadExpenseReportExpenses(currentExpenseReport);
            });
        },
        error : function(e) {
            console.log('error addEligibleChargeToReport ' + JSON.stringify(e))
        }
    });
}

function buildExpandingPreview(hasReceiptImage, previewDiv, expense) {
    var expensesNodeWidth = expensesNode.outerWidth();
    var prev = document.createElement('DIV');
    prev = $(prev);
    prev.addClass('previewDiv');

    // first, lets see if the records already in the array
    var tmpRec = null;
    for (var i = 0; i < previewedNodes.length; i++) {
        if (previewedNodes[i].expenseId == expense.id) {
            tmpRec = previewedNodes[i];
            break;
        }
    }

    if (tmpRec == null) {
        tmpRec = {
            expenseId: expense.id,
            node : previewDiv,
            visible : false,
            toggle:function() {
                if (!this.visible) {
                    this.show();
                }
                else {
                    this.hide();
                }
            },
            show:function() {
                this.node.show();
                this.visible = true;
                console.log('show(): this.visible=' + this.visible)
            },
            hide:function() {
                this.node.hide();
                this.visible = false;
                console.log('hide(): this.visible=' + this.visible)
            }
        };
        previewedNodes.push(tmpRec);
    }


    if (expense.receipt != null) {
        var nUrl = expenseReceiptImagePreviewUrl.replace('receiptId', '' + expense.id) + ('?uid=' + new Date().getTime() + '' );

        prev.html('<IMG class="preview" src ="' + nUrl + '" width ="' + previewDiv.width() + '"  />');
        previewDiv.append(prev);
        tmpRec.node = previewDiv;
        if (tmpRec.visible != true)
            tmpRec.hide(); /// safe default

        $(hasReceiptImage).click(function() {
            tmpRec.toggle();
        });
    }
    return tmpRec;
}

function scale(srcWidth, srcHeight, maxWidth, maxHeight) {

    var resizeWidth = srcWidth;
    var resizeHeight = srcHeight;

    var aspect = resizeWidth / resizeHeight;

    // simple cheat test
    if (srcWidth == maxWidth)
        return { height : srcHeight, width: srcWidth };

    if (resizeWidth > maxWidth) {
        resizeWidth = maxWidth;
        resizeHeight = resizeWidth / aspect;
    }

    if (resizeHeight > maxHeight) {
        aspect = resizeWidth / resizeHeight;
        resizeHeight = maxHeight;
        resizeWidth = resizeHeight + aspect;
    }
    return { width : resizeWidth , height : resizeHeight };
}

function loadImageSize(imgSrc, cb) {
    var newImg = new Image();
    newImg.src = imgSrc;
    var height = newImg.height;
    var width = newImg.width;
    p = $(newImg).load(function() {
        var x = {width: newImg.width, height: newImg.height};
        console.log(JSON.stringify(x));
        cb(x);
        return x;
    });
}

function drawExpense(expensesNode, ec) {

    var ecId = expenseNodeId(ec.id);
    var menu = '<SPAN class ="menu"><span class ="rowSpacer"></span> ';
    menu += '<span class ="addReceipt"><span class="hasReceiptIcon" ></span></span>';
    menu += '</SPAN> ';

    $(expensesNode).append("<div id='" + ecId + "' class = 'expense'> <span class ='merchant'> " +
            ec.merchant + "</span>" + "<span class ='amount'>" +
            renderPrice(ec.amount) + "</span>" +
            " <span class='category'>" + ec.category + "</span> " + menu + " <span class = 'progress'><img src ='" +
            loaderImageSrc + "' /></span><div class ='receiptPreview'></div></div>"
    );

    var hasReceiptImg = $('#' + ecId + ' .hasReceiptIcon');
    hasReceiptImg.attr('title', receiptText);

    var hasReceipt = ec.receipt != null && ec.receipt != null;

    if (!hasReceipt && ec.flag == 'receiptRequired') {
        hasReceiptImg.addClass('hasReceiptFalse');
    }

    if (hasReceipt) {
        hasReceiptImg.addClass('hasReceiptTrue');
    }

    var cb = createExpenseDropzoneCallback(currentExpenseReport, ec.id);
    var ceExpenseNode = $('#' + ecId);
    ceExpenseNode.bind('mouseover', cb);
    ceExpenseNode.bind('dragenter', cb);

    var previewNode = $('#' + ecId + ' .receiptPreview');
    var hideMePreviewNode = buildExpandingPreview(hasReceiptImg, previewNode, ec);
    ceExpenseNode.draggable({
        revert : 'invalid',
        start : function () {
            movingExpensesToCharges = true;
            currentlyDraggedExpenseId = ec.id;
            var v = hideMePreviewNode.visible;
            hideMePreviewNode.hide();
            hideMePreviewNode.visible = v;
            console.log('start(): node.visible=' + hideMePreviewNode.visible)

        },
        stop : function () {
            movingExpensesToCharges = false;
            currentlyDraggedExpenseId = null;
            console.log('moving expenses to charges == false');
            loadExpenseReportExpensesLocally(report.id, report.purpose, report.expenses, function() {
                for (var i = 0; i < previewedNodes.length; i++) {
                    var pn = previewedNodes [i];
                    console.log('expenseId: ' + pn.expenseId + ' is visible? ' + (pn.visible ? 'y' : 'n'));
                }
            });
        },
        drag : function () {
        }
    });


}


function buildCssSprite(node, on, off) {
    var hri = node;
    hri.addClass(off)
            .mouseover(function() {
                hri.addClass(on);
                hri.removeClass(off);
            })
            .mouseout(function() {
                hri.addClass(off);
                hri.removeClass(on);
            });
}

function renderPrice(p) {
    p = p + '';
    if (p.indexOf(".") != -1) {
        var np1 = p.split(".")[0];
        var np2 = p.split(".")[1].substring(0, 2);
        p = np1 + "." + np2;
    }
    return '<span class = "dollar">$</span> ' + p;
}

function loadEligibleCharges(callback) {
    $.ajax({
        url: eligibleChargesUrl,
        success: function(data) {
            callback(data);
        }
    });
}

function createExpenseReportIfRequired(purpose, cb) {
    var wrappedCallback = function() {
        cb(purpose, currentExpenseReport);
    };
    if (currentExpenseReport == null) {
        $.post(createErUrl, { purpose :  purpose }, function(result) {
            console.log('creating report ' + purpose + ':' + result);
            currentExpenseReport = parseInt(result);
            wrappedCallback();
        });
    } else {
        wrappedCallback();
    }
}

function addEligibleChargeToReport(ec, cb) {
    createExpenseReportIfRequired(newReportLabel, function(purpose, reportId) {

        var url = createExpenseReportExpenseUrl.replace('reportId', reportId);

        var obj = { chargeId : ec.id   };

        $.ajax({
            type : 'POST',
            url : url,
            cache : false,
            dataType : 'json',
            data : {  chargeId  :  ec.id },
            success : function(result) {
                cb(purpose, reportId);
            },
            error : function(e) {
                console.log('error addEligibleChargeToReport ' + JSON.stringify(e))
            }
        });

    });
}


function refreshMenus(erIdToSelect) {

    $.ajax({
        type : 'GET',
        url : openReportsUrl ,
        cache : false,
        dataType : "json",
        success : function(result) {

            var menu = expenseReportMenu;
            var menuOptions = menu.options;

            menu.options.length = 0;

            menuOptions[0] = new Option("", "", true, false);

            $(result).each(function(e) {
                var expense = result[e];
                var selectMenuId = expense.id;
                var mIndex = menuOptions.length;
                menuOptions[menuOptions.length] = new Option(expense.purpose, "" + selectMenuId, true, false);
                if (erIdToSelect != null) {
                    if (erIdToSelect == selectMenuId)
                        menu.selectedIndex = mIndex;
                }
            });
        },
        error : function(e) {
            console.log('error addEligibleChargeToReport ' + JSON.stringify(e))
        }
    });
}

function createExpenseDropzoneCallback(reportId, expenseId) {
    return function() {
        currentFocusedExpenseElement = this;
        currentExpense = expenseId;
        if (isDragging && !movingChargeToExpenses != false && movingExpensesToCharges != false) {
            console.log("dragging onto expense ID #" + expenseId);
        }
    };
}


function setupDropzone() {

    dropzoneNode.filedrop({

        maxfilesize : 20,
        fallback_id: 'upload_button'  ,
        url: receiptsUrl ,
        paramname: 'file',
        data: {
            reportId : function() {
                return currentExpenseReport;
            },
            expenseId:  function() {
                return currentExpense;
            },
            name : 'file'
        },
        error: function(err, file) {
            console.log(JSON.stringify(err));
            switch (err) {
                case 'BrowserNotSupported':
                    alert('browser does not support html5 drag and drop')
                    break;
                case 'TooManyFiles':
                    // user uploaded more than 'maxfiles'
                    break;
                case 'FileTooLarge':
                    // program encountered a file whose size is greater than 'maxfilesize'
                    // FileTooLarge also has access to the file which was too large
                    // use file.name to reference the filename of the culprit file
                    break;
                default:
                    break;
            }
        },
        dragOver: function() {

            // user dragging files over #dropzone
        },
        dragLeave: function() {
            // user dragging files out of #dropzone
            fileDragging(false);
        },
        docOver: function() {
            fileDragging(true);
        },
        docLeave: function() {
            fileDragging(false);
            // user dragging files out of the browser document window
        },
        drop: function() {
            console.log('fileDrop!drop()');
            fileDragging(false);
            console.log("drop is finished");
            // user drops file
        },
        uploadStarted: function(i, file, len) {
            console.log('started uploading file ' + i + ' of ' + len + ' ' + file);
            $('#' + currentFocusedExpenseElement.id + ' .progress').show();
        },
        uploadFinished: function(i, file, response, time) {
            $('#' + currentFocusedExpenseElement.id + ' .progress').hide();
        },
        progressUpdated: function(i, file, progress) {
            // this function is used for large files and updates intermittently
            // progress is the integer value of file being uploaded percentage to completion
        },
        speedUpdated: function(i, file, speed) {
            // speed in kb/s
        },
        rename: function(name) {
            // name in string format
            // must return alternate name as string
        },
        beforeEach: function(file) {
            // file is a file object
            // return false to cancel upload
        },
        afterAll: function() {
            console.log('finished uploading (afterAll())');
            $('#' + currentFocusedExpenseElement.id + ' .progress').hide();
            loadExpenseReportExpenses(currentExpenseReport)
        }
    });

}



$(function() {

    previewNode = $('#preview');
    attachReceiptImageSrc = $('#arImg').attr('src');
    loaderImageSrc = $('#loaderImg').attr('src');

    console.log('the image url is ' + attachReceiptImageSrc);

    dropzoneNode = $('#dropzone');
    ecsBalance = $('#ecsBalance');
    expensesNode = $('#expenses');
    ecsNode = $('#ecs');
    expenseReport = $('#expenseReportPanel');
    purposeReport = $("#reportPurpose");
    purposeReport.html(newReportLabel);

    setupDropzone();

    var trashIconNode = $('#trashIcon');
    trashIconNode.click(function() {
        deleteExpenseReport(currentExpenseReport);
    });

    buildCssSprite($('#trashIcon'), 'trashIconOn', 'trashIconOff');

    purposeReport.editInPlace({
        callback: function(originalElement, html, original) {
            createExpenseReportIfRequired(html, function(p, cid) {

                var newUrl = reportPurposeUrl.replace('reportId', currentExpenseReport);

                $.ajax({
                    type : 'POST',
                    url : newUrl,
                    cache : false,
                    dataType : 'json',
                    data : {  title  :  html },
                    success : function(result) {

                        refreshMenus(currentExpenseReport);
                    },
                    error : function(e) {
                        alert('error couldnt update purpose ' + JSON.stringify(e))
                    }
                });

                console.log('the results: ' + html);
                report.purpose = html;
                console.log('the purpose: ' + report.purpose);

            });

            return (html);
        }
    });

    ecsNode.droppable({
        accept : '.expense',
        drop: function(event, ui) {
            console.log('something attempted a drop on the eligible charges pallete');
            if (!movingExpensesToCharges) {
                return;
            }
            console.log("moving the expense back to the charges.");
            restoreExpenseToEligibleCharges(currentlyDraggedExpenseId);
        }
    });

    expenseReport.droppable({
        accept : '.ec',
        drop: function(event, ui) {
            console.log('something attempted a drop on the expenses pallete');
            if (!movingChargeToExpenses) {
                return;
            }
            console.log('moving charges back to expenses');
            fileDragging(false);
            var draggable = ui.draggable;
            var ec = currentEligibleCharge;
            addEligibleChargeToReport(ec, function(purpose, currentExpenseReportId) {
                loadExpenseReportExpenses(currentExpenseReportId);
                renderEligibleCharges();
            });
        }
    });

    expenseReportMenu = document.getElementById("openExpenses");
    $(expenseReportMenu).bind('change', function() {
        if (this.value != '') {
            var eri = parseInt(this.value);
            loadExpenseReportExpenses(eri);
        }
    });

    $('#createNewReportButton').bind('click', function(e) {
        createNewExpenseReport();
    });

    refreshMenus();
    renderEligibleCharges();
    resetExpenseReport();
});

function createNewExpenseReport() {
    currentExpenseReport = null;
    createExpenseReportIfRequired(newReportLabel, function(p, erId) {
        loadExpenseReportExpenses(erId);
    });
}

function renderEligibleCharges(cb) {
    console.log('rendering eligible charges');
    loadEligibleCharges(function(data) {

        ecsNode.html("");
        var total = 0.0;
        for (var i = 0; i < data.length; i++) {
            drawEligibleCharge(ecsNode, data[i]);
            total += data[i].amount;
        }

        ecsBalance.html(renderPrice(total));
        if (cb != null) {
            cb();
        }
    });
}