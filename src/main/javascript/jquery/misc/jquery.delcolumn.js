
(function ($) {
    $.fn.addDelColumn = function () {
	console.info("Plug-in colonne suppression chargé");
        this.each(function() {
            $(this).append('<td class="delLine"><button><img alt="Delete" src="images/btn_del.png"/></button></td>');
            var opath  = $(this).attr("data-path");
            $(this).children("td.delLine").on("click.addDelColumn.askdel", function(e) {
		e.stopPropagation();
		$("<div class='delpopup'>Confirmer la suppression<br/></div>")
			.append("<button id='addDelColumn_Confirmer'>Confirmer</button>")
			.append("<button id='addDelColumn_Annuler'>Annuler</button>")
			.appendTo($("body"));
		$("div.delpopup button#addDelColumn_Confirmer").on("click.addDelColumn.del", function () {
			var delp = {url: "/massey/html/call.gdo",
				data : {p: opath, c: "Unplug"}};
			console.info("Suppression de l’entrée " + opath);
			boCall(delp).done(function () {alert("Suppression effectuée");});
			$("button#addDelColumn_Annuler").trigger("click.addDelColumn.cancel");
		});
		$("div.delpopup button#addDelColumn_Annuler").on("click.addDelColumn.cancel", function() {
			$("div.delpopup").detach();});
	    });
        })
	return this;
    }
}) (jQuery);
