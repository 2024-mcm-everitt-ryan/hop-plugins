package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

public class FormDataBuilder {

    public static final class Builder {
        private Control control;
        private int margin;
        private FormAttachment left;
        private FormAttachment right;

        private Builder() {
        }

        public static Builder buildFormData() {
            return new Builder();
        }

        public Builder control(Control control) {
            this.control = control;
            return this;
        }

        public Builder margin(int margin) {
            this.margin = margin;
            return this;
        }

        public Builder left(int numerator, int offset) {
            this.left = new FormAttachment(numerator, offset);
            return this;
        }

        public Builder right(int numerator, int offset) {
            this.right = new FormAttachment(numerator, offset);
            return this;
        }

        public FormData build() {
            FormData e = new FormData();
            e.left = this.left;
            e.right = this.right;
            e.top = new FormAttachment(control, margin);
            return e;
        }
    }
}
