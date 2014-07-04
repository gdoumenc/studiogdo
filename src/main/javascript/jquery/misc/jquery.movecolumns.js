/*
*   Move columns in a table
*   Copyright (C) 2012  StudioGDO
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU Affero General Public License as
*   published by the Free Software Foundation, either version 3 of the
*   License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Affero General Public License for more details.
*
*   You should have received a copy of the GNU Affero General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

(function ($) {
    $.fn.setReorder = function () {
        var table = this;
        var headers = this.find("thead th");
        var table_rows = this.find("tr");
        headers.on("mousedown.setReorder", function () {
            var fromidx = $(this).index();
            headers.css("cursor","move");
            headers.not($(this)).one("mouseup.moveColumn", function () {
                var toidx = $(this).index();
                if (toidx != fromidx) {
                    if ((toidx + 1)== headers.length) {
                        table_rows.each(function() {
                            $(this).children("th,td").eq(fromidx).detach().insertAfter(
                                $(this).children("th,td").eq(toidx - 1)
                            );
                        });
                    } else {
                        table_rows.each(function() {
                            $(this).children("th,td").eq(fromidx).detach().insertBefore(
                                $(this).children("th,td").eq(toidx)
                            );
                        });
                    }
                }
                headers.off("mouseup.moveColumn"); headers.css("cursor","pointer");
            });
            $(this).one("mouseup", function () {$("table thead th").off("mouseup.moveColumn")});
        });
        return this;
    }}) (jQuery);
    
$("table").setReorder();
