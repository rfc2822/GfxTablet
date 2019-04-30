namespace GfxTabletWinDotnet
{
    partial class GfxTabletTestdialog
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.txtX = new System.Windows.Forms.TextBox();
            this.txtY = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.txtPressure = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.txtButton = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.closeButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 13);
            this.label1.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(17, 17);
            this.label1.TabIndex = 0;
            this.label1.Text = "X";
            // 
            // txtX
            // 
            this.txtX.Location = new System.Drawing.Point(84, 13);
            this.txtX.Margin = new System.Windows.Forms.Padding(4);
            this.txtX.Name = "txtX";
            this.txtX.ReadOnly = true;
            this.txtX.Size = new System.Drawing.Size(132, 22);
            this.txtX.TabIndex = 1;
            // 
            // txtY
            // 
            this.txtY.Location = new System.Drawing.Point(84, 45);
            this.txtY.Margin = new System.Windows.Forms.Padding(4);
            this.txtY.Name = "txtY";
            this.txtY.ReadOnly = true;
            this.txtY.Size = new System.Drawing.Size(132, 22);
            this.txtY.TabIndex = 3;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 45);
            this.label2.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(17, 17);
            this.label2.TabIndex = 2;
            this.label2.Text = "Y";
            // 
            // txtPressure
            // 
            this.txtPressure.Location = new System.Drawing.Point(84, 77);
            this.txtPressure.Margin = new System.Windows.Forms.Padding(4);
            this.txtPressure.Name = "txtPressure";
            this.txtPressure.ReadOnly = true;
            this.txtPressure.Size = new System.Drawing.Size(132, 22);
            this.txtPressure.TabIndex = 5;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 77);
            this.label3.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(65, 17);
            this.label3.TabIndex = 4;
            this.label3.Text = "Pressure";
            // 
            // txtButton
            // 
            this.txtButton.Location = new System.Drawing.Point(84, 109);
            this.txtButton.Margin = new System.Windows.Forms.Padding(4);
            this.txtButton.Name = "txtButton";
            this.txtButton.ReadOnly = true;
            this.txtButton.Size = new System.Drawing.Size(132, 22);
            this.txtButton.TabIndex = 7;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(12, 109);
            this.label4.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(49, 17);
            this.label4.TabIndex = 6;
            this.label4.Text = "Button";
            // 
            // closeButton
            // 
            this.closeButton.Location = new System.Drawing.Point(84, 145);
            this.closeButton.Name = "closeButton";
            this.closeButton.Size = new System.Drawing.Size(75, 23);
            this.closeButton.TabIndex = 8;
            this.closeButton.Text = "Close";
            this.closeButton.UseVisualStyleBackColor = true;
            this.closeButton.Click += new System.EventHandler(this.closeButton_Click);
            // 
            // GfxTabletTestdialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(236, 180);
            this.Controls.Add(this.closeButton);
            this.Controls.Add(this.txtButton);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.txtPressure);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.txtY);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txtX);
            this.Controls.Add(this.label1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "GfxTabletTestdialog";
            this.Text = "Debug";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.GfxTabletTestdialog_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txtX;
        private System.Windows.Forms.TextBox txtY;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txtPressure;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox txtButton;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button closeButton;
    }
}

