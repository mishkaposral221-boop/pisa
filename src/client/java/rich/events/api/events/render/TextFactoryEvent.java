package rich.events.api.events.render;

import rich.events.api.events.Event;

public class TextFactoryEvent implements Event {
   private String text;

   public void replaceText(String var1, String var2) {
      if (this.text != null && !this.text.isEmpty()) {
         if (this.text.contains(var1)
            && (
               this.text.equalsIgnoreCase(var1)
                  || this.text.contains(var1 + " ")
                  || this.text.contains(" " + var1)
                  || this.text.contains("⏏" + var1)
                  || this.text.contains(var1 + "§")
            )) {
            this.text = this.text.replace(var1, var2);
         }
      }
   }

   public void setText(String var1) {
      this.text = var1;
   }

   public String getText() {
      return this.text;
   }

   public TextFactoryEvent(String var1) {
      this.text = var1;
   }
}
