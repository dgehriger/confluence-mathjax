(function (jQuery) {
   class ActivateStringFields {
      constructor() {
      }

      // this is run before the information is saved from the macro browser
      beforeParamsRetrieved(params) {
         if (params.hasOwnProperty("equation")) {
            params.equation = params.equation.replace(/\{/g, '__OPENBRACE__').replace(/\}/g, '__CLOSEBRACE__');
         }
         return params;
      }

      // this is run before the information is displayed in the macro browser
      beforeParamsSet(params, inserting) {
         if (params.hasOwnProperty("equation")) {
            params.equation = params.equation.replace(/__OPENBRACE__/g, '{').replace(/__CLOSEBRACE__/g, '}');
         }
         return params;
      }
   }

   ActivateStringFields.prototype.fields = {
      "string": function (param, options) {
         let paramDiv;
         if (param.name == "equation") {
            paramDiv = jQuery(MacroBrowser.StringFields.textarea());
         } else {
            paramDiv = jQuery(MacroBrowser.StringFields.text());
         }
         let input = jQuery(":input", paramDiv);

         if (param.required) {
            input.keyup(AJS.MacroBrowser.processRequiredParameters);
         }

         return AJS.MacroBrowser.Field(paramDiv, input, options);
      },
   };

   AJS.toInit(function () {
      AJS.bind("init.rte", function () {
         AJS.MacroBrowser.activateStringFields = function (macroName) {
            AJS.MacroBrowser.setMacroJsOverride(macroName, new ActivateStringFields());
         }
         AJS.MacroBrowser.activateStringFields('mathjax-inline-macro');
      });
   });
})(jQuery);
