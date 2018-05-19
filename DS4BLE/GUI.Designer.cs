namespace DS4BLE
{
    partial class GUI
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
            this.cameraOutput = new System.Windows.Forms.PictureBox();
            this.TakePicture = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.cameraOutput)).BeginInit();
            this.SuspendLayout();
            // 
            // cameraOutput
            // 
            this.cameraOutput.Location = new System.Drawing.Point(12, 12);
            this.cameraOutput.Name = "cameraOutput";
            this.cameraOutput.Size = new System.Drawing.Size(400, 300);
            this.cameraOutput.TabIndex = 0;
            this.cameraOutput.TabStop = false;
            // 
            // TakePicture
            // 
            this.TakePicture.Location = new System.Drawing.Point(438, 24);
            this.TakePicture.Name = "TakePicture";
            this.TakePicture.Size = new System.Drawing.Size(147, 42);
            this.TakePicture.TabIndex = 1;
            this.TakePicture.Text = "Snap Picture";
            this.TakePicture.UseVisualStyleBackColor = true;
            this.TakePicture.Click += new System.EventHandler(this.TakePicture_Click);
            // 
            // GUI
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(597, 450);
            this.Controls.Add(this.TakePicture);
            this.Controls.Add(this.cameraOutput);
            this.Name = "GUI";
            this.Text = "GUI";
            ((System.ComponentModel.ISupportInitialize)(this.cameraOutput)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.PictureBox cameraOutput;
        private System.Windows.Forms.Button TakePicture;
    }
}