INSERT INTO resume_templates (template_id, name, description, format_type, template_content)
VALUES (
    'classic',
    'Classic',
    'A clean and professional LaTeX resume template with a modern layout',
    'latex',
    '\\documentclass[letterpaper,11pt]{article}

\\usepackage{latexsym}
\\usepackage[empty]{fullpage}
\\usepackage{titlesec}
\\usepackage{marvosym}
\\usepackage[usenames,dvipsnames]{color}
\\usepackage{verbatim}
\\usepackage{enumitem}
\\usepackage[hidelinks]{hyperref}
\\usepackage{fancyhdr}
\\usepackage[english]{babel}
\\usepackage{tabularx}
\\input{glyphtounicode}

\\pagestyle{fancy}
\\fancyhf{} % clear all header and footer fields
\\fancyfoot{}
\\renewcommand{\\headrulewidth}{0pt}
\\renewcommand{\\footrulewidth}{0pt}

% Adjust margins
\\addtolength{\\oddsidemargin}{-0.5in}
\\addtolength{\\evensidemargin}{-0.5in}
\\addtolength{\\textwidth}{1in}
\\addtolength{\\topmargin}{-.5in}
\\addtolength{\\textheight}{1.0in}

\\urlstyle{same}

\\raggedbottom
\\raggedright
\\setlength{\\tabcolsep}{0in}

% Sections formatting
\\titleformat{\\section}{
  \\vspace{-4pt}\\scshape\\raggedright\\large
}{}{0em}{}[\\color{black}\\titlerule \\vspace{-5pt}]

% Ensure that generate pdf is machine readable/ATS parsable
\\pdfgentounicode=1

%-------------------------
% Custom commands
\\newcommand{\\resumeItem}[1]{
  \\item\\small{
    {#1 \\vspace{-2pt}}
  }
}

\\newcommand{\\resumeSubheading}[4]{
  \\vspace{-2pt}\\item
    \\begin{tabular*}{0.97\\textwidth}[t]{l@{\\extracolsep{\\fill}}r}
      \\textbf{#1} & #2 \\\\
      \\textit{\\small#3} & \\textit{\\small #4} \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeSubSubheading}[2]{
    \\item
    \\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}
      \\textit{\\small#1} & \\textit{\\small #2} \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeProjectHeading}[2]{
    \\item
    \\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}
      \\small#1 & #2 \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeSubItem}[1]{\\resumeItem{#1}\\vspace{-4pt}}

\\renewcommand\\labelitemii{$\\vcenter{\\hbox{\\tiny$\\bullet$}}$}

\\newcommand{\\resumeSubHeadingListStart}{\\begin{itemize}[leftmargin=0.15in, label={}]}
\\newcommand{\\resumeSubHeadingListEnd}{\\end{itemize}}
\\newcommand{\\resumeItemListStart}{\\begin{itemize}}
\\newcommand{\\resumeItemListEnd}{\\end{itemize}\\vspace{-5pt}}

\\begin{document}

\\begin{center}
    \\textbf{\\Huge \\scshape {{NAME}}} \\\\ \\vspace{1pt}
    \\small {{PHONE}} $|$ \\href{mailto:{{EMAIL}}}{\\underline{{{EMAIL}}}} $|$ {{ADDRESS}}
\\end{center}

\\section{Education}
{{EDUCATION_SECTION}}

\\section{Experience}
{{EXPERIENCE_SECTION}}

\\section{Skills}
{{SKILLS_SECTION}}

\\end{document}'
);

INSERT INTO resume_templates (template_id, name, description, format_type, template_content)
VALUES (
    'compact',
    'Compact',
    'A modern and compact LaTeX resume template with clean typography and efficient use of space',
    'latex',
    '\\documentclass[11pt]{article}

\\usepackage[T1]{fontenc}
\\usepackage{inter} 
\\renewcommand*\\familydefault{\\sfdefault}
\\usepackage[none]{hyphenat}
\\usepackage{geometry}
\\geometry{
    a4paper,
    top=1.8cm,
    bottom=1in,
    left=2.5cm,
    right=2.5cm
}

\\setcounter{secnumdepth}{0} % remove section numbering
\\pdfgentounicode=1 % make ATS friendly

\\usepackage{enumitem}
\\setlist[itemize]{
    noitemsep,
    left=2em
}
\\setlist[description]{itemsep=0pt}
\\setlist[enumerate]{align=left}
\\usepackage[dvipsnames]{xcolor}
\\colorlet{icnclr}{gray}
\\usepackage{titlesec}
\\titlespacing{\\subsection}{0pt}{*0}{*0}
\\titlespacing{\\subsubsection}{0pt}{*0}{*0}
\\titleformat{\\section}{\\color{Sepia}\\large\\fontseries{black}\\selectfont\\uppercase}{}{}{\\ruleafter}[\\global\\RemVStrue]
\\titleformat{\\subsection}{\\fontseries{bold}\\selectfont}{}{}{\\rvs}
\\titleformat{\\subsubsection}{\\color{gray}\\fontseries{bold}\\selectfont}{}{}{}

\\usepackage{xhfill} 
\\newcommand\\ruleafter[1]{#1~\\xrfill[.5ex]{1pt}[gray]} % add rule after title in .5 x-height 

\\newif\\ifRemVS % remove vspace between \\section & \\subsection
\\newcommand{\\rvs}{
    \\ifRemVS
        \\vspace{-1.5ex}
    \\fi
    \\global\\RemVSfalse
}

\\usepackage{fontawesome5}

\\usepackage[bookmarks=false]{hyperref} % [imp!]
\\hypersetup{ 
    colorlinks=true,
    urlcolor=Sepia,
    pdftitle={My Resume},
}

\\usepackage[page]{totalcount}
\\usepackage{fancyhdr}
\\pagestyle{fancy}
\\renewcommand{\\headrulewidth}{0pt}	
\\fancyhf{}							

\\usepackage{amsmath}
\\usepackage{amsfonts}

\\begin{document}

%== HEADER ==%
\\begin{center}      
    {\\fontsize{28}{28}\\selectfont {{NAME}}} \\\\ \\bigskip

    {\\color{icnclr}\\faMapMarker} {{ADDRESS}} \\quad 
    {\\color{icnclr}\\faEnvelope[regular]} \\href{mailto:{{EMAIL}}}{{{EMAIL}}} \\quad
    {\\color{icnclr}\\faIcon{mobile-alt}} \\href{tel:{{PHONE}}}{{{PHONE}}} 
\\end{center}

\\section{Education}
{{EDUCATION_SECTION}}

\\section{Experience}
{{EXPERIENCE_SECTION}}

\\section{Skills}
{{SKILLS_SECTION}}

\\end{document}'
);