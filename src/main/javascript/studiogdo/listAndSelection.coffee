define ["studiogdo/listCellForm", "studiogdo/listSelectionForm"], (DefaultCellForm, DefaultSelectionForm) ->
 
    # 2 div list renderer (list + selected item)
    class ListAndSelection
        
        constructor: (@list, @selected, @list_skel, @selected_skel, @CellForm, @SelectionForm) ->
            @path = null                    # path for the list
            @selectedPath = null            # path for the selected item
        
            @bDataTable = true              # list set as a datatable
        
            @sSelectionClass = "selected"   # css class of the TR selected
            @sSelectionEvent = "click"      # event for TR selection
        
            @sEditionEvent = "dblclick"     # event for TD edition
            @jCellForm = null               # the current cell edition form
            @jSelectionForm = null          # the current selection form
        
        # shows the selected line
        selectLine: (selected_tr) =>
            for tr in @list.querySelectorAll "tr"
                tr.classList.remove(@sSelectionClass)
            selected_tr.classList.add(@sSelectionClass)
                
        # shows the selected item
        showSelected: (data) =>
            @showSelectedCallback(null, data)
    
        showSelectedCallback: (evt, data) =>
            
            # creates js selection form
            if !@jSelectionForm?
                if @SelectionForm?
                    @jSelectionForm = new @SelectionForm(@)
                else
                    @jSelectionForm = new DefaultSelectionForm(@)
                
            # gets selection path
            if evt?
                evt.preventDefault()
                tr = getParentByTagName(evt.target, "TR", true)
                @selectLine(tr)
                path = tr.dataset.apath
            else
                tr = @list.querySelector "tr.#{@sSelectionClass}"
                if tr?
                    path = tr.dataset.apath
                else
                    path = @jSelectionForm.path
                
            # calls bo if path defined
            return if !path?
            @selectedPath = path
            @showSelectedCallbackBOCall(evt, data)
        
        # may be redefine if specific BOCall should be done on selection
        showSelectedCallbackBOCall: (evt, data) =>
            bocall = new BOCall
            bocall.done = => @showSelectedBOCallback(bocall, evt)
            bocall.postFacet(@selectedPath, @jSelectionForm.skel, "html5", data)
            
        showSelectedBOCallback: (bocall, evt) =>
            
            # reset event handler
            #
            # ICICICICICCI
            #
            # $(evt.delegateTarget).one(@sSelectionEvent, @showSelectedCallback) if evt?
                if !@selected?
                        alert 'No selection holder defined for the list'
                        return
            
                # sets selected content
                $(@selected).html(bocall.responseText) if bocall?
                @jSelectionForm.showSelectedCallback(@selected) if @jSelectionForm?

        # closes the selected item
        closeSelected: =>
            @closeSelectedCallback()
            
        closeSelectedCallback: =>
            @jSelectionForm = null
            @selected.innerHTML = ""
        
        # commit the selected item
        commitSelected: (map) =>
             @commitSelectedCallback(map)

        # updates the selection changes
        commitSelectedCallback: (map) =>
            data = new FormData(@selected.querySelector("form"))
            for key, value of map
                data.append(key, value)
            @showList(@path, data, @closeSelected)
        
        # shows the edit form associated to the selected item
        inlineEditSelectedCallback: (evt) =>
            td = evt.currentTarget

            # reset edition handler
            $(td).one(@sEditionEvent, @inlineEditSelectedCallback)

            # get span
            span = td.querySelector("span")
            return if !span?
            #return if @jCellForm?

            form = td.querySelector("form")
            return if !form?

            # shows form if no content in span (cannot click on it)
            form.classList.remove("hidden") if span.innerText == ''
            
            # close previous open edition form
            @jCellForm?.close()

            # create new one
            if @CellForm?
                @jCellForm = new @CellForm(@, span, form)
            else
                @jCellForm = new DefaultCellForm(@, span, form)
                @jCellForm.firstFocus()
                @jCellForm.commit = (evt, list) =>
                    fdata = new FormData(form)
                    @showList(@path, fdata)
                    @jCellForm = null
            
        # shows the list
        showList: (path, data, after) =>

            # set list path
            @path ?= path if path?
            path ?= @path

            # if the list is already there (don't call BO)
            if !@list_skel?
                @showListBOCallback()
                return
                
            # gets the list from BO  
            bocall = new BOCall
            bocall.done = => 
                @showListBOCallback(bocall)
                after?()
            bocall.postFacet(path, @list_skel, "html5", data)
        
        showListCallback: (oSettings) =>
            
            nbaff = @list.querySelector("#nbaff span").innerHTML if @list.querySelector("#nbaff span")?
            nbsel = @list.querySelector("#nbsel span").innerHTML if @list.querySelector("#nbsel span")?
            twarn = @list.querySelector("#table_limit_warn")
            if (nbaff != nbsel)
                twarn.innerHTML = "Attention, la limite d’affichage a été atteinte (#{nbaff} lignes maximum)" if twarn?
            
            for tr in $("tbody tr, tfoot tr", @list) when !$(tr).hasClass("initialized")

                # sets selection event handler (one to avoid bouncing event)
                if @sSelectionEvent? && @selected_skel?
                    $(tr).on(@sSelectionEvent, @showSelectedCallback)
                
                # sets edition event handler
                if @sEditionEvent?
                    for td in $("td.editable", tr)
                        $(td).one(@sEditionEvent, @inlineEditSelectedCallback) # one fo double click not click twice
            
                # sets euro columns (format content)
                for span in $("td.euro span, td span.euro", tr)
                    val = $(span).text()
                    $(span).text(numeral(val).format('0,0 $'))

                # sets cent columns (format content)
                for span in $("td.cent span, td span.cent", tr)
                    val = $(span).text() /100
                    $(span).text(numeral(val).format('0,0.00 $'))

                # sets date columns (format content)
                for span in $("td.date span, td span.date", tr)
                    date = moment($(span).text())
                    if date.isValid()
                            moment($(span).text()).lang()
                            $(span).text(date.format('L'))

                # sets time columns (format content)
                for span in $("td.time span, td span.time", tr)
                    date = moment($(span).text(), ['HH:mm', 'HH:mm:ss', 'hh:mm', 'hh:mm:ss'])
                    if date.isValid()
                            $(span).text(date.format('LT'))

                # sets datetime columns (format content)
                for span in $("td.datetime span, td span.datetime", tr)
                    date = moment($(span).text())
                    if date.isValid()
                            $(span).text(date.format('LLL'))

                # sets boolean columns (adds icon)
                for span in $("td.boolean span", tr)
                    $(span).hide()
                    i = $("<i></i>").appendTo($(span).parent())
                    val = $(span).text()
                    if val == 'true'
                        i.addClass("icon-ok")
                    else
                        i.addClass("icon-plus-sign")
                        
                $(tr).addClass("initialized")

        showListBOCallback: (bocall) =>
            @list.innerHTML = bocall.responseText if bocall? && @list?
            @selected.innerHTML = "" if @selected?
            @setDataTable()
         
        # sets the list as a dataTable
        setDataTable: =>
            if @bDataTable
                table = $("table", @list)
                table = $("table", @list.parentNode) if table.length == 0
                table.dataTable(@fnSettings())
            else 
                @showListCallback()
                
        fnSettings: =>
                "aaSorting": [ ]
                'aLengthMenu': [[10, 25, 100, 500, -1], [10, 25, 100, 500, 'Tous']]
                "bStateSave": true
                "bLengthChange" : true
                "iDisplayLength": 10
                "fnDrawCallback": @showListCallback
                "sDom": "Tlfrtip<'clear'>"
                "sPaginationType": "full_numbers"
                "oLanguage":
                        "sLengthMenu": "Afficher _MENU_ lignes par page",
                        "sZeroRecords": "Pas trouvé..",
                        "sInfo": "Affichage de _START_ à _END_ sur _TOTAL_ lignes",
                        "sInfoEmpty": "Affichage de 0 à 0 sur 0 lignes",
                        "sInfoFiltered": "(filtrage sur _MAX_ lignes)",
                        "sSearch" : "Rechercher"
                        "oPaginate":
                            "sFirst" : "&nbsp;&nbsp;&nbsp;"
                            "sLast" : "&nbsp;&nbsp;&nbsp;"
                            "sNext" : "&nbsp;&nbsp;&nbsp;"
                            "sPrevious" : "&nbsp;&nbsp;&nbsp;"

        addDeleteButtons: (msg, where) =>
            ###
            Adds delete button on each line.
            Should be added after showListCallback
                showListCallback: (oSettings) =>
                super(oSettings)
                @addDeleteButtons()
            ###
            
            msg ?= "Voulez-vous réellement faire cette action?"
            where ?= "tbody tr > td:last-child"
            
            askConfirmation = (evt) =>
                if evt?
                    evt.stopImmediatePropagation()
                    evt.preventDefault() 
                res = confirm "Confirmation : #{msg}"
                if res
                    @deleteCallback(evt)
                
            for td in @list.querySelectorAll(where) when !(td.classList.contains("dataTables_empty") || td.classList.contains("delete_button"))
                button = document.createElement('BUTTON')
                button.setAttribute('style', 'float: right;')
                button.innerHTML = '<img alt="Détruire" title="Détruire" src="/shared/css/images/btn_del.png"/>'
                td.appendChild(button)
                td.classList.add("delete_button")
    
                button.addEventListener("click", askConfirmation, true)
         
        deleteCallback: (evt) =>
            button = evt.currentTarget
            tr = getParentByTagName(button, "TR")
            bocall = new BOCall
            bocall.applyCommand(tr.dataset.apath, "Unplug")
            bocall.done = =>
                @deleteBOCallback(bocall)
                
        deleteBOCallback: (bocall) =>
            @showList(@path)

        
        class BooleanEditForm extends DefaultCellForm
            constructor: ->
                super
                @firstFocus()
                
            commit: (evt, list) =>
                list.editForm = null
                td = getParentByTagName(evt.target, "TD", true)
                if !td.classList.contains("boolean")
                    super
                    return
                bo = new BOCall
                bo.done = =>
                    @value.innerHTML = bo.responseText
                input = if evt.target.tagName=='INPUT' then evt.target else evt.target.querySelector('INPUT')
                bo.appendBOParam("i_#{td.dataset.apath}", input.value)
                bo.postProp(td.dataset.apath)
    
        class CentEditForm extends DefaultCellForm
            constructor: ->
                super
                @firstFocus()
                
            commit: (evt, list) =>
                list.editForm = null
                td = getParentByTagName(evt.target, "TD", true)
                if !td.classList.contains("cent")
                    super
                    return
                bo = new BOCall
                bo.done = =>
                    @value.innerHTML = bo.responseText
                    #centToEuro(@value, @value);
                input = if evt.target.tagName=='INPUT' then evt.target else evt.target.querySelector('INPUT')
                bo.appendBOParam("i_#{td.dataset.apath}", input.value)
                bo.postProp(td.dataset.apath)
    
