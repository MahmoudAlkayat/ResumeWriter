from flask import Flask, request, send_file
import os
import tempfile
import subprocess
import uuid

app = Flask(__name__)

@app.route('/convert', methods=['POST'])
def convert_latex_to_pdf():
    try:
        # Get LaTeX content from request
        latex_content = request.get_data().decode('utf-8')
        
        # Create temporary directory
        with tempfile.TemporaryDirectory() as temp_dir:
            # Generate unique filename
            filename = str(uuid.uuid4())
            tex_path = os.path.join(temp_dir, f"{filename}.tex")
            pdf_path = os.path.join(temp_dir, f"{filename}.pdf")
            
            # Write LaTeX content to file
            with open(tex_path, 'w') as f:
                f.write(latex_content)
            
            # Run pdflatex
            result = subprocess.run([
                'pdflatex',
                '-interaction=nonstopmode',
                '-output-directory=' + temp_dir,
                tex_path
            ], capture_output=True, text=True)
            
            if result.returncode != 0:
                return {'error': 'PDF generation failed', 'details': result.stderr}, 500
            
            # Check if PDF was generated
            if not os.path.exists(pdf_path):
                return {'error': 'PDF file not generated'}, 500
            
            # Return the PDF file
            return send_file(
                pdf_path,
                mimetype='application/pdf',
                as_attachment=True,
                download_name='resume.pdf'
            )
            
    except Exception as e:
        return {'error': str(e)}, 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000) 