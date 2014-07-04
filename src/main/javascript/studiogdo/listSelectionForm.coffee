define ->

    class ListSelectionForm
        
        constructor: (@list) ->
            @skel = @list.selected_skel   # usefull for creation (if same as selection)
            
        showSelectedCallback: (selected) =>
            form = selected.querySelector("form")
            @path = form.dataset.apath if form?
            
            $("#annuler").on("click", @cancel)
            $("#valider").on("click", @commit)
            $("#editer").on("click", @lock)
            $("#liberer").on("click", @unlock)
        
        cancel: (evt) => 
            evt?.preventDefault()
            @list.closeSelected()

        # lock the selected stencil (the button must have the $Locked path for unlocking the stencil)
        commit: (evt) => 
            evt?.preventDefault()
            if $("#liberer").length
                map = {}
                map["s_#{evt.currentTarget.dataset.apath}"]= ""
                @list.commitSelected(map)
            else
                @list.commitSelected()

        # lock the selected stencil (the button must have the $Locked path)
        lock: (evt) =>
            evt?.preventDefault()
            data = new FormData
            data.append("s_JExvY2tlZA==", BOCall.connected)
            @list.showSelected(data)
            
        # unlock the selected stencil (the button must have the $Locked path)
        unlock: (evt) =>
            evt?.preventDefault()
            data = new FormData
            data.append("s_JExvY2tlZA==", "")
            @list.showSelected(data)
            

