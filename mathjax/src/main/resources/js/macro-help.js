// This closure helps us keep our variables to ourselves.
// This pattern is known as "iife" - immediately invoked function expression

// Start closure
macroHelpDocumentation = (function( jQuery ){

   // module variables
   var methods = new Object();
   var initialAuiBlanketZIndex = "";
   var embeddedHelpMacroId     = "my-embedded-macro-help";
   var macroBrowserId          = "macro-browser-dialog";

   // module methods
   methods[ 'getMacroHelp' ] = function( e, pluginId, restVersion, macroId ){
      // Prevent any attached event handlers from executing as that can result
      // in opening unwanted windows or closing the macro browser
      e.preventDefault();
      e.stopPropagation();
      jQuery( e.target ).unbind();

      // Use ajax to retrieve the documentation from the plugin's rest service, append
      // the returned html to the end of the document and show the dialog.
      // Note, the z index of the aui-blanket (the semi-transparent gray layer) needs
      // to be moved up so it's now in front of the macro browser but behind the
      // documentation dialog.
      var helpUrl = AJS.Data.get( "base-url" ) + "/rest/"+pluginId+"/"+restVersion+"/help/" + macroId;
      jQuery.ajax({
         url: helpUrl,
         type: "GET",
         dataType: "json",
      }).done( function ( data ) {
         //alert( data[ "message-body" ] );
         var macroBrowserZIndex;

         // append the returned html to the body of the page
         jQuery( "body" ).append( data[ "message-body" ] );

         // give the last section an id
         jQuery( "section" ).last().attr( "id", embeddedHelpMacroId );

         // show the help dialog
         AJS.dialog2( "#"+embeddedHelpMacroId ).show();

         // move the dialog on top of the macro browser dialog
         macroBrowserZIndex = Number(jQuery( "#"+macroBrowserId ).css("z-index"));
         jQuery( "#"+embeddedHelpMacroId ).css("z-index", (macroBrowserZIndex + 500) );

         // move the aui-blanket between the macro browser and the help dialog
         initialAuiBlanketZIndex = Number(jQuery( ".aui-blanket" ).css("z-index"));
         jQuery( ".aui-blanket" ).css("z-index", (macroBrowserZIndex + 250) );

      }).fail( function (self, status, error ){ alert( error );
      });
   }

   methods[ 'closeMacroHelp' ] = function(){
      AJS.dialog2("#"+embeddedHelpMacroId).remove();
      // put the aui-blanket back
      jQuery( ".aui-blanket" ).attr( 'style', 'z-index: '+initialAuiBlanketZIndex );
      jQuery( ".aui-blanket" ).attr( 'aria-hidden', 'false' );
   }

   // return the object with the methods
   return methods;

// End closure
})( jQuery );
