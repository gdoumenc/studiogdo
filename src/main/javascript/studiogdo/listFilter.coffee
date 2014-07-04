define ->
  
  # @filter : DOM container
  # @filter_skel : skel file
  # @startShow : default show mode at start (false -> close, true -> open)
  # @form : DOM filter form
  
  class ListFilter
    
    constructor: (@filter, @filter_skel)->
      @startShow = false
      
    # shows the filter
    showFilter: =>
      return if !@filter?
      bocall = new BOCall
      bocall.done = => @filterBOCallback(bocall)
      bocall.postFacet(null, @filter_skel, "html5")
   
    filterBOCallback: (bocall) =>
      @filter.innerHTML = bocall.responseText
      @form = $("form", @filter).get(0)
      
      $('#plus', @filter).on "click", (evt) =>
        evt?.preventDefault()
        @show(true)
      $('#moins', @filter).on "click", (evt) =>
        evt?.preventDefault()
        @reset()
        @filterCallback(evt)
        @show(false)
      $('#go', @filter).on "click", (evt) =>
        evt?.preventDefault()
        @filterCallback(evt)
        @show(false)
      $(@form).on('submit', @filterCallback)
      $(@form).on('keypress', @onKeypress)
      
      for label in $('label') when label.htmlFor != ''
         elem = document.getElementById(label.htmlFor)
         elem?.label = label
               
      @show(@startShow)
       
    filterCallback: (evt) =>
      evt?.preventDefault()
      alert 'filer to be done'
       
    reset: =>
      for input in $("input", @form) when input.id != 'nbre_ligne'
        input.value = ''
      for select in $("select", @form)
        for option in $("option", select)
          option.selected = false
        select.querySelector("option").selected = true
  
    show: (all) =>
      
      hide = (elt) ->
        $(elt).hide()
        $(elt.label).hide()
        $($(elt).parent().get(0).label).hide()  # if label is defined on parent
      show = (elt) ->
        $(elt).show()
        $(elt.label).show()
        $($(elt).parent().get(0).label).show()  # if label is defined on parent
  
      for input in $("INPUT", @form)
        if (input.getAttribute("type") == "checkbox")
          if !all && input.getAttribute("checked") != "checked"
            hide(input)
          else
            show(input) if input.id != ""
        else
          if !all && input.value == ""
            hide(input)
          else
            show(input)
     
      for select in $("select", @form)
        if !all && select.querySelectorAll("option[selected]").length == 0
          hide(select)
        else
          show(select)
      
      # hides all br if not all
      for br in $("br", @form)
        if all then $(br).show() else $(br).hide()
    
    # filter on key enter
    onKeypress: (evt) =>
      @filterCallback(evt) if evt.which == 13
