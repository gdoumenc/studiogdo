// Generated by CoffeeScript 1.6.3
(function() {
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  define(function() {
    var ListSelectionForm;
    return ListSelectionForm = (function() {
      function ListSelectionForm(list) {
        this.list = list;
        this.unlock = __bind(this.unlock, this);
        this.lock = __bind(this.lock, this);
        this.commit = __bind(this.commit, this);
        this.cancel = __bind(this.cancel, this);
        this.showSelectedCallback = __bind(this.showSelectedCallback, this);
        this.skel = this.list.selected_skel;
      }

      ListSelectionForm.prototype.showSelectedCallback = function(selected) {
        var form;
        form = selected.querySelector("form");
        if (form != null) {
          this.path = form.dataset.apath;
        }
        $("#annuler").on("click", this.cancel);
        $("#valider").on("click", this.commit);
        $("#editer").on("click", this.lock);
        return $("#liberer").on("click", this.unlock);
      };

      ListSelectionForm.prototype.cancel = function(evt) {
        if (evt != null) {
          evt.preventDefault();
        }
        return this.list.closeSelected();
      };

      ListSelectionForm.prototype.commit = function(evt) {
        var map;
        if (evt != null) {
          evt.preventDefault();
        }
        if ($("#liberer").length) {
          map = {};
          map["s_" + evt.currentTarget.dataset.apath] = "";
          return this.list.commitSelected(map);
        } else {
          return this.list.commitSelected();
        }
      };

      ListSelectionForm.prototype.lock = function(evt) {
        var data;
        if (evt != null) {
          evt.preventDefault();
        }
        data = new FormData;
        data.append("s_JExvY2tlZA==", BOCall.connected);
        return this.list.showSelected(data);
      };

      ListSelectionForm.prototype.unlock = function(evt) {
        var data;
        if (evt != null) {
          evt.preventDefault();
        }
        data = new FormData;
        data.append("s_JExvY2tlZA==", "");
        return this.list.showSelected(data);
      };

      return ListSelectionForm;

    })();
  });

}).call(this);

/*
//@ sourceMappingURL=listSelectionForm.map
*/