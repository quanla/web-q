$.fn.focusWithoutScrolling = function(){
  var x = window.scrollX, y = window.scrollY;
  this.focus();
  window.scrollTo(x, y);
//  alert(x + ", " + y);
  return this;
};

var dialogCloseBtn = function(text) {
	return  {
	    text: text,
	    click: function() { $(this).dialog("close"); }
	};
};

var OK_DIALOG = dialogCloseBtn("Ok");
var CANCEL_DIALOG = dialogCloseBtn("Cancel");
var CLOSE_DIALOG = dialogCloseBtn("Close");


function addCommas(str) {
	if (typeof str == "string") {
		str = parseFloat(str);
	}
	
	if (str % 1 != 0) {
		str = str.toFixed(2);
	}
    var amount = String(str);
    amount = amount.split(".");
    return addCommasInt(amount[0]) + (amount.length > 1 ? "." + amount[1] : "");
}
function addCommasInt(str) {
    var amount = String(str);
    amount = amount.split("").reverse();

    var output = "";
    for ( var i = 0; i <= amount.length-1; i++ ){
        output = amount[i] + output;
        if (amount[i+1]=='-') {
        	continue;
        }
        if ((i+1) % 3 == 0 && (amount.length-1) !== i)output = ',' + output;
    }
    return output;
}

function randomString(length, special) {
   var iteration = 0;
   var randomString = "";
   var randomNumber;
   if(special == undefined){
       special = false;
   }
   while(iteration < length){
       randomNumber = (Math.floor((Math.random() * 100)) % 94) + 33;
       if(!special){
           if ((randomNumber >=33) && (randomNumber <=47)) { continue; }
           if ((randomNumber >=58) && (randomNumber <=64)) { continue; }
           if ((randomNumber >=91) && (randomNumber <=96)) { continue; }
           if ((randomNumber >=123) && (randomNumber <=126)) { continue; }
       }
       iteration++;
       randomString += String.fromCharCode(randomNumber);
   }
   return randomString;
}



function formatMonth(time) {
	var date = new Date(time);
	return formatN(date.getMonth() + 1, "00") + "/" + date.getFullYear();
}

function formatDate(time) {
	var date = new Date(time);
	return date.getFullYear() + "/" + formatN(date.getMonth() + 1, "00") + "/" + formatN(date.getDate(), "00");
}
function formatTime(time) {
	var date = new Date(time);
	return formatDate(time) + " " + formatN(date.getHours(), "00") + ":" + formatN(date.getMinutes(), "00") + ":" + formatN(date.getSeconds(), "00");
}
function formatTime2(time) {
	var date = new Date(time);
	return formatN(date.getHours(), "00") + ":" + formatN(date.getMinutes(), "00");
}
function formatN(original, format) {
	var ret = Math.round(original*100)/100;
	if (format.indexOf("000") > -1 && ret < 100) {
		ret = "0" + ret;
	}
	if (format.indexOf("00") > -1 && ret < 10) {
		ret = "0" + ret;
	}
//	if (format.indexOf("0") > -1 && ret < 1) {
//		ret = "0" + ret;
//	}
	return ret;
}

function dialogWarning(text) {
	return $("<p/>").html(
		"<span class=\"ui-icon ui-icon-alert\" style=\"float:left; margin:0 7px 20px 0;\"></span>"+
		text);
}

var JSON = JSON || {};
//implement JSON.stringify serialization
JSON.stringify = JSON.stringify || function (obj) {
    var t = typeof (obj);
    if (t != "object" || obj === null) {
        // simple data type
        if (t == "string") obj = '"'+obj+'"';
        return String(obj);
    }
    else {
        // recurse array or object
        var n, v, json = [], arr = (obj && obj.constructor == Array);
        for (n in obj) {
            v = obj[n]; t = typeof(v);
            if (t == "string") v = '"'+v+'"';
            else if (t == "object" && v !== null) v = JSON.stringify(v);
            json.push((arr ? "" : '"' + n + '":') + String(v));
        }
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
    }
};
//implement JSON.parse de-serialization
JSON.parse = JSON.parse || function (str) {
    if (str === "") str = '""';
    eval("var p=" + str + ";");
    return p;
};

function show(id) {
	var div = $("#" + id);
	var display = div.css("display");
	if (display=="none") {
		var url = div.attr("url");
		if (url!=null) {
			$.get(url, function(data) {
				div.html(data);
				div.slideDown('slow');
				div.removeAttr();
			}, "html");
		} else {
			div.slideDown('slow');
		}
	} else {
		div.slideUp('slow');
	}
}

function p0(f, param) {
	return function () {
		f(param);
	};
}

function addMap(target, map) {
	if (target==null) {
		return map;
	}
	for (key in target) {
		map[key] = target[key];
	}
	return map;
}

function checking(comp, text) {
	comp.html("<img src='/img/checking.gif' height='15' />" + (text? " " + text : ""));
}
function correct(comp, text) {
	comp.html("<img src='/img/correct.jpg' height='15' />" + (text? " " + text : ""));
}
function wrong(comp, text) {
	comp.html("<img src='/img/wrong.jpg' height='15' />" + (text? " " + text : ""));
}

function wrongF(comp) {
	return function (data,textStatus, errorThrown) {
		wrong(comp, textStatus);
	};
}

function postJson(url, values, onSuccess, onError) {
	if ("function"==(typeof onError)) {
		;
	} else if (onError) {
		checking(onError);
	}
	
	var customizedOnError = function(errorThrown) {
		if (onError==null) {
			;
		} else if ("function"==(typeof onError)) {
			onError(errorThrown);
		} else {
			wrong(onError, errorThrown);
		}
	};
	$.ajax({
		type: 'POST',
		url: url,
		data: values,
		success: function(data, textStatus, jqXHR) {
			if (data==null) {
				customizedOnError("Invalid server response");
			} else {
				onSuccess(data);
			}
		},
		error: function(data,textStatus, errorThrown) {
			customizedOnError(errorThrown);
		},
//		complete: function (jqXHR, textStatus) {
//			alert("complete");
//		},
//		dataFilter: function(data, type) {
//			alert("dataFilter");
//		},
		dataType: "json"
	});
}
function getJson(url, onSuccess, onError) {
	if ("function"==(typeof onError)) {
		;
	} else if (onError) {
		checking(onError);
	}
	
	var customizedOnError = function(errorThrown) {
		if (onError==null) {
			;
		} else if ("function"==(typeof onError)) {
			onError(errorThrown);
		} else {
			wrong(onError, errorThrown);
		}
	};
	$.ajax({
		type: 'GET',
		url: url,
		success: function(data, textStatus, jqXHR) {
			if (data==null) {
				customizedOnError("Invalid server response");
			} else {
				onSuccess(data);
			}
		},
		error: function(data,textStatus, errorThrown) {
			customizedOnError(errorThrown);
		},
//		complete: function (jqXHR, textStatus) {
//			alert("complete");
//		},
//		dataFilter: function(data, type) {
//			alert("dataFilter");
//		},
		dataType: "json"
	});
}

function percent(num) {
	return Math.round(num * 1000) / 10;
}

function ceil (num) {
	return Math.ceil(num * 100) / 100;
}

function allNumbers(val) {
	return val.replace(/\D/g, "");
}
function allWords(val) {
	return val.replace(/\W/g, "");
}

function trim(val) {
	if (!val) {
		return null;
	}
	
	return val.replace(/^\s+/, "").replace(/\s+$/, "");
}

function formatMoney(x) {
	if (x==null) {
		return "0";
	}
    var parts = x.toString().split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
//    return parts.join(".");
    return parts[0];
}

var Fs = Fs || {};
Fs.p0 = function(p1, a) {
	return function() {
		p1(a);
	}
};

Fs.invokeAll = function(funcs, data1, data2) {
	for (var i in funcs) {
		funcs[i](data1, data2);
	}
};

Fs.invokeChecks = function(funcs, data) {
	for (var i in funcs) {
		if (funcs[i](data)) {
			return true;
		}
	}
	return false;
};

Fs.cache = function(f0) {
	var invoked = false;
	var cachedData = null;
	return function() {
		if (!invoked) {
			cachedData = f0();
			invoked = true;
		}
		
		return cachedData;
	};
};

Fs.sequel = function (fs) {
	return function() {
		for (var i in fs) {
			fs[i]();
		}
	};
};

Fs.tail1 = function(func, b, c) {
	return function(a) {
		return func(a, b, c);
	};
};
Fs.tail2 = function(func, c, d) {
	return function(a, b) {
		return func(a, b, c, d);
	};
};

Fs.invoke = function(func) {
	if ((typeof func) == "function") {
		return func();
	} else {
		return func;
	}
};

var Cols = Cols || {};
Cols.values = function(map) {
	var ret = [];
	for ( var k in map) {
		ret.push(map[k]);
	}
	return ret;
};
Cols.find = function(col, func) {
	for (var i in col) {
		var e = col[i];
		if (func(e)) {
			return e;
		}
	}
	return null;
};


Cols.yield = function(col, func) {
	var ret = [];
	for (var i in col) {
		var e = func(col[i]);
		if (e != null) {
			ret.push( e );
		}
	}
	return ret;
};
Cols.filter = function(col, func) {
	var ret = [];
	for (var i in col) {
		if (func(col[i])) {
			ret.push( col[i] );
		}
	}
	return ret;
};
Cols.join = function(col, delimiter) {
	var ret = "";
	for (var i in col) {
		if (ret.length > 0) {
			ret += delimiter;
		}
		ret += col[i];
	}
	return ret;
};
Cols.merge = function(map1, map2) {
	for ( var k in map2) {
		map1[k] = map2[k];
	}
};

//Cols.eachLine = function(/*Collection<F>*/ steps, /*P2<F,P1<N>>*/ digF, /*P1<List<N>>*/ resultF) {
//	eachLine(steps, digF, /*new LinkedList<N>()*/[], resultF);
//}

Cols.eachLine = function(/*final List<F>*/ steps, /*final P2<F,P1<N>>*/ digF, /*final List<N>*/ collecteds, /*final P1<List<N>>*/ resultF) {
	
	if (steps.length == 0) {
		resultF(collecteds);
		return;
	}

	var feed = steps[0];
	digF(feed, function(n) {
		var newCollecteds = Cols.copy(collecteds);
		newCollecteds.push(n);
		Cols.eachLine(steps.slice(1, steps.length), digF, newCollecteds, resultF);
	});
};

Cols.each = function(col, p1) {
//	alert("aa");
	for (var i = 0; i<col.length; i++) {
		p1(col[i]);
	}
};

/**
 * collect(ele, total)
 */
Cols.collect = function(col, init, collect) {
	var total = init;
	for (var i in col) {
		total = collect(col[i], total);
	}
	return total;
};
Cols.sum = function(col, getNum) {
	return Cols.collect(col, 0, function(e, sum) {
        var val = (getNum ? getNum(e) : e);
        if (val == null) {
            return sum;
        }
        return val + sum;
	});
};

/**
 * p2<Element,P0 onDone> 
 */
Cols.eachPar = function(col, p2) {
	Cols.eachPar1(0, col, p2);
};
Cols.eachPar1 = function(index, col, p2) {
	if (index >= col.length) {
		return;
	}
	
	p2(col[index], function() {
		Cols.eachPar1(index+1, col, p2);
	});
};


Cols.indexOf = function(ele, col, colExtract) {
	for (var i in col) {
		if (colExtract(col[i]) == ele) {
			return i;
		}
	}
	return -1;
};

Cols.copy = function(arr1) {
	var ret = [];
	
	for (var i in arr1) {
		ret.push(arr1[i]);
	}
	return ret;
};
Cols.eachChildRecursive = function(/*A*/ a,
		/*F1<A, Collection<A>>*/ digF,
		/*P1<A>*/ p1) {
	var col = digF(a);
	if (col==null) {
		return;
	}
	for (var childI in col) {
		var child = col[childI];
		p1(child);
		Cols.eachChildRecursive(child, digF, p1);
	}
};

Cols.addList = function(key, value, maps) {
	var list = maps[key];
	if (list == null) {
		list = [];
		maps[key] = list;
	}
	list.push(value);
};

Cols.isEmpty = function(col) {
	return col == null || col.length == 0;
};

Cols.isNotEmpty = function(col) {
	return !Cols.isEmpty(col);
};

Cols.addAll = function (from, to) {
	for (var i in from) {
		to.push(from[i]);
	}
};

Cols.addRemove = function(col) {
	return function(item) {
		col.push(item);
		return function() {
			col.splice(col.indexOf(item), 1);
		}
	}
};

Cols.toEnd = function(array) {
	var i = array.length + 1;
	return function() {
		if (i > 1) {
			i--;
		}
		return array[array.length-i];
	}
};

Cols.remove = function(e, col) {
	var i = col.indexOf(e);
	if (i == -1) {
		return;
	}
	col.splice(i, 1);
};

Cols.removeBy = function(col, f) {
    for (var j = 0; j < col.length; j++) {
        var obj = col[j];
        if (f(obj)) {
            col.splice(j, 1);
        }
    }
};
Cols.removeAll = function(col, list) {
	for (var i in col) {
		var item = col[i];
		var rowI = list.indexOf(item);
		
		if (rowI == -1) {
//			alert("Can not find");
			return;
		}
		list.splice(rowI, 1);
	}
};

Cols.sortBy = function(byF) {
	var nullGoLast = true;
	return function(rd1, rd2) {
		var by1 = byF(rd1);
		var by2 = byF(rd2);

		if (by1 == null) {
			return by2 == null ? 0 : (nullGoLast ? 1 : -1);
		} else if (by2 == null) {
			return nullGoLast ? -1 : 1;
		}

		if ((typeof by1) == "string" ) {
			if (by1 < by2)
			     return -1;
			  if (by1 > by2)
			    return 1;
			  return 0;
		}
		return by1 - by2;
	};
};

Cols.index = function(col, by) {
    if (typeof by == "string") {
        var byAttr = by;
        by = function(ele) { return ele[byAttr];};
    }

    return Cols.collect(col, {}, function(ele, groups) {
        var index = by(ele);
        var list = groups[index];
        if (list == null) {
            list = [];
            groups[index] = list;
        }
        list.push(ele);
        return groups;
    });
};
Cols.group = function(col, by) {
    return Cols.values(Cols.index(col, by));
};



var Async = Async || {};


/**
 * var oneRun = Async.oneRun();
 * 
 * var run1 = oneRun();
 * var run2 = oneRun();
 * // Async post
 * alert(run1())
 * alert(run1())
 * alert(run2())
 */
Async.oneRun = function() {
	var lastRunRef = [null];
	return function() {
		if (lastRunRef[0] != null) {
			lastRunRef[0][0] = false;
		}
		var lastRun = [true];
		lastRunRef[0] = lastRun;
		
		return function() {
			var r = lastRun[0];
			lastRun[0] = false;
			return r;
		}
	}
};

Async.ladyFirst = function() {
	var afterLadyDone = null;
	var freeToGo = false;
	return {
		ladyDone: function() {
			freeToGo = true;
			if (afterLadyDone) {
				afterLadyDone();
			}
		},
		manTurn: function(func) {
			if (freeToGo) {
				func();
			} else {
				afterLadyDone = func;
			}
		}
	};
};

/**
 * @return checkF(checkIndex);
 */
Async.runWhenAllChecked = function(checkCount, func) {
	var flags = new Array(checkCount);
	for (var i=0;i<checkCount;i++) {
		flags[i] = false;
	}
	return function(chechIndex) {
		flags[chechIndex] = true;
		
		for (var i=0;i<checkCount;i++) {
			if (flags[i] == false) {
				return;
			}
		}
		
		func();
	}
};

Async.runAfterCount= function(checkCount, func) {
	return function(chechIndex) {
		checkCount --;
		
		if (checkCount <= 0) {
			func();
		}
	}
};

/**
 * func(quantity) => interrupted
 * @param func
 */
Async.incrementalRepeater = function(func) {
	var quantityFF = function() {
		var i = 0;
		var array = [1, 2, 5, 10, 20, 50, 100, 200, 500, 1000];
		return function() {
			i++;
			return array[Math.min(parseInt(i / 10), array.length-1)];
		}
	};
	
	var createRunner = function() {

		var sleepTimeF = Cols.toEnd([400, 300, 300, 300, 200, 200, 100]);
		var quantityF = quantityFF();
		var alive = true;
		// Start
		var run = function() {
			if (!alive) {
				return;
			}
			var interrupted = func(quantityF());
			if (interrupted) {
				alive = false;
				return;
			}
			setTimeout(run, sleepTimeF());
		};
		run();
		
		return {
			stop: function() {
				alive = false;
			}
		};
	};
	var runner = null;
	return {
		start: function() {
			if (runner != null) {
				return;
			}
			runner = createRunner();
		},
		stop: function() {
			if (runner != null) {
				runner.stop();
				runner = null;
			}
		}
	};
};

/**
 * checkF(val, stillValid)
 * @return invoke(val)
 */
Async.lazyValidate = function(startF, checkF) {
	var validating = [null];
	return function(val) {
		if (val == null) {
			alert("Async.lazyValidate: Not support null value");
			return;
		}
		var thisValidate = [val];
		if (validating[0] != null) {
			if (validating[0][0] == val) {
				// This val is being validated (not done)
				return;
			}
			validating[0][0] = null;
		}
		validating[0] = thisValidate;
		var stillValid = function() {
			return thisValidate[0] != null;
		};
		
		startF();
		setTimeout(function() {
			if (!stillValid()) {
				return;
			}
			checkF(val, stillValid);
		}, 500);
	}
};

function moneyInput(input) {
	var onChange = function() {
		input.val( formatMoney(input.val().replace(/,/g, '')) );
	};
	numberInput(input, {
		min: 1,
		step: 1000,
		change: onChange
	});
}
/**
 * options: {min, step, change, max()}
 * @param input
 * @param options
 * @returns
 */
function numberInput(input, options) {
	// Capture change event
	input.otherOnchange = null;
	var onChange = function() {
		if (options && options.change) options.change();
		if (input.otherOnchange != null) {
			input.otherOnchange();
		}
	};
	input.jQueryOnChange = input.change;
	input.change = function(func) {
		input.otherOnchange = func;
	};
	input.jQueryOnChange(onChange);
	
	
	input.click(function() {input.select()});

	var stepValue = options && options.step ? options.step : 1;
	input.keydown(function(event) {
		if (event.keyCode == 38) { // Up
			var newVal = input.val().toString().replace(/,/g,'')*1 + stepValue;
			
			if (options && options.max != null && newVal > options.max()) {return;}
			input.val(newVal);
			
			onChange();
		} else if (event.keyCode == 40) { // Down
			var newVal = input.val().toString().replace(/,/g,'')*1 - stepValue;
			
			if (options && options.min != null && newVal < options.min) {
				return;
			}
			
			input.val(newVal);

			onChange();
		} else if (event.keyCode == 13) { // Enter
			focusNextInput(input);
		}
    });
	return input;
}

function focusNextInput(input) {
	$(":input:eq(" + ($(":input").index(input)*1 + 1) + ")").select();
}

function onEnter(input, func) {
	input.keypress(function(e) {
        if(e.which == 13) {
            return func();
        }
        return true;
    });
}

function onAlt(key, input, func) {
	input.keypress(function(e) {
		
		if(e.altKey===true && e.which == key) {
			e.preventDefault();
			func();
		}
	});
}

function toString(o, depth) {
	if (depth == null) {
		depth = 1;
	}
	if (depth == 0) {
		return "" + o;
	}
	if (o==null) {
		return "null";
	} else if (typeof(o) == "object") {
		if (o.length) {
			var str = "[";
			for (var a in o) {
				if (str.length > 1) {
					str += ",";
				}
				var v = o[a];
				str += toString(v, depth-1);
			}
			return str + "]";
		} else {
			var str = "{";
			for (var a in o) {
				if (str.length > 1) {
					str += ",";
				}
				var v = o[a];
				str += "\"" + a + "\":" + toString(v, depth-1);
			}
			return str + "}";
		}
	} else {
		return "\"" + o + "\"";
	}
}

function evalO(str) {
	return eval("[" + str + "]")[0];
}

function onlyNumbers(input) {
	input.keydown(function(event) {
        // Allow: backspace, delete, tab, escape, and enter
        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 || 
             // Allow: Ctrl+A
            (event.ctrlKey === true || event.metaKey === true) || 
             // Allow: home, end, left, right
            (event.keyCode >= 35 && event.keyCode <= 39)) {
                 // let it happen, don't do anything
                 return;
        }
        else {
            // Ensure that it is a number and stop the keypress
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                event.preventDefault(); 
            }   
        }
    });
}

var StringUtil = StringUtil || {};
StringUtil.uppercaseFirstChar = function(str) {
	return str.substring(0, 1).toUpperCase() + str.substring(1, str.length).toLowerCase();
};
StringUtil.isBlank = function(val) {
	if (isString(val)) {
		return val==null || val.replace(/\s/g, "").length == 0;
	} else {
		return val == null;
	}
};

StringUtil.isEmpty = function(val) {
	return val==null || val == '';
};
StringUtil.isNotEmpty = function(val) {
	return !StringUtil.isEmpty(val);
};
StringUtil.randomString = randomString;

var DateUtil = DateUtil || {};

DateUtil.SECOND_LENGTH = 1000;
DateUtil.MINUTE_LENGTH = 60 * DateUtil.SECOND_LENGTH;
DateUtil.HOUR_LENGTH = 60 * DateUtil.MINUTE_LENGTH;
DateUtil.DAY_LENGTH = 24 * DateUtil.HOUR_LENGTH;
DateUtil.YEAR_LENGTH = 365 * DateUtil.DAY_LENGTH;

DateUtil.yesterday = function() {
	return DateUtil.addDays(new Date(), -1 );
};

DateUtil.DAY_LENGTH = 24*60*60*1000;

DateUtil.addDays = function(date1, days) {
	var date = new Date(date1.getTime());
	date.setDate(date.getDate() + days);
	return date;
};
DateUtil.addMonth = function(date1, month) {
	var date = new Date(date1.getTime());
	date.setMonth(date.getMonth() + month);
	return date;
};
DateUtil.addMinutes = function(date1, minutes) {
	var date = new Date(date1.getTime());
	date.setMinutes(date.getMinutes() + minutes);
	return date;
};
DateUtil.format2digits = function(num) {
	num = "" + num;
	if (num.length == 1) {
		return "0" + num;
	}
	return num;
};
DateUtil.format = function(date, format) {
	return format
	.replace(/yyyy/g, date.getFullYear())
	.replace(/MM/g, DateUtil.format2digits(date.getMonth()+1))
	.replace(/dd/g, DateUtil.format2digits(date.getDate()))
	.replace(/HH/g, DateUtil.format2digits(date.getHours()))
	.replace(/mm/g, DateUtil.format2digits(date.getMinutes()))
	;
};

DateUtil.sameDay = function(d1, d2) {
    return DateUtil.truncate(d1).getTime() == DateUtil.truncate(d2).getTime();
};

DateUtil.parse = function(str, format) {
    if (format=="yyyy_MM_dd") {
        var m = /(\d+)_(\d+)_(\d+)/.exec(str);
        return new Date(m[1], m[2] - 1, m[3]);
    }
    if (format=="yyyy_MM") {
        var m = /(\d+)_(\d+)/.exec(str);
        return new Date(m[1], m[2] - 1);
    }
    throw "Unsupported format: " + format;
};

DateUtil.dayOfWeek = function(day) {
	if (day == 0) {
		return "Chủ nhật";
	}
	return "Thứ " + (day+1); 
};

DateUtil.isToday = function(date) {
	return DateUtil.truncate(date).getTime() == DateUtil.truncate(new Date()).getTime();
};

DateUtil.dayEnd = function(date) {
	return new Date(DateUtil.truncate(DateUtil.addDays(date, 1)).getTime() - 1);
};
DateUtil.monthEnd = function(date) {
    return new Date(DateUtil.truncateMonth(DateUtil.addMonth(date, 1)).getTime() - 1);
};
DateUtil.truncate = function(date) {
	return new Date(date.getFullYear(), date.getMonth(), date.getDate());
};
DateUtil.truncateHour = function(date) {
	return new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), 0, 0);
};
DateUtil.truncateMonth = function(date) {
	return new Date(date.getFullYear(), date.getMonth(), 1, 0, 0, 0);
};

DateUtil.weekBegin = function(date) {
	var dow = date.getDay();
	
	return new Date(date.getFullYear(), date.getMonth(), date.getDate() - dow, 0, 0, 0);
};


var TimingUtil = TimingUtil || {};

TimingUtil.syncDelay = function (f, delay) {
	delay = delay || 1000;
	
	var globalInterrupted = null;
	return function() {
		if (globalInterrupted) {
			globalInterrupted[0] = true;
		}
		var interrupted = [false];
		globalInterrupted = interrupted;
		setTimeout(function() {
			if (!interrupted[0]) {
				f();
			}
		}, delay);
	};
};

var LangUtil = LangUtil || {};
LangUtil.booleanValue = function(o) {
    if (o == null) {
        return false;
    }

    if (o == false || o == true) {
        return o;
    }

    if (typeof o == "string") {
        return o != "false";
    }

    return true;
};
LangUtil.toNum = function(o) {
    if (o == null) {
        return null;
    }

    return o * 1;
};


var ObjectUtil = ObjectUtil || {};

ObjectUtil.equals = function (o1, o2) {
	if (o1 == null) {
		return o2 == null;
	}
	
	if (o2 == null) {
		return false;
	}
	
	if ((typeof o1) != (typeof o2)) {
		return false;
	}
	
	if (typeof o1 != "object") {
		return o1 == o2;
	}
	
	if (o1.length != o2.length) {
		return false;
	}
	
	for (var i in o1) {
		if (!ObjectUtil.equals(o1[i], o2[i])) {
			return false;
		}
	}
	for (var i in o2) {
		if (!ObjectUtil.equals(o1[i], o2[i])) {
			return false;
		}
	}

    return true;
};

ObjectUtil.copy = function(fromO, toO) {
	for (var name in fromO) {
		toO[name] = fromO[name];
	}
};
ObjectUtil.clone = clone;
ObjectUtil.clear = function(obj) {
    for (prop in obj) {
//        if (obj.hasOwnProperty(prop)) {
            delete obj[prop];
//        }
    }
};
ObjectUtil.hasValue = function(o) {
    if (o == null) {
        return false;
    }
    for (var i in o) {
        if (o.hasOwnProperty(i)) {
            return true;
        }
    }
    return false;
}

var Http = Http || {};
Http.afterSharp = function() {
	var href = window.location.href;
	var index = href.indexOf("#");
	if (index == -1) {
		return null;
	}
	return href.substring(index + 1);
};

function clone(obj) {
	if (obj.length == null) {
		return jQuery.extend(true, {}, obj);
	} else {
		var ret = [];
		for ( var i in obj) {
			ret[i] = clone(obj[i]);
		}
		return ret;
	}
}

function isString(obj) {
	return typeof obj == "string";
}


function fixFloat(num) {
	return Math.round(num * 1000000) / 1000000;
}

//alert(fixFloat(0.000001));