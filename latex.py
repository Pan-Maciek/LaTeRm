latex_code = '\u001b[Y'

def printLatex(latex):
    print(f"\u001b[93m{latex_code}{latex}{latex_code}\u001b[0m")

def printLoop():
    while True:
        print("\u001b[92m")
        print("-" * 64)
        print("\u001b[0m", end = '')
        latex = input("Hand me some LaTeX -> ")
        print()
        print(f"There You go! \u001b[93m\u001b[Y{latex}\u001b[Y\u001b[0m")


shrodinger = r'- \frac{{\hbar ^2 }}{{2m}}\frac{{d^2 \psi (x)}}{{dx^2 }} + U(x)\psi (x) = E\psi (x)'

maxwell = r'\oint_C {E \cdot d\ell  =  - \frac{d}{{dt}}} \int_S {B_n dA}'

convolution = r'x(n) * y(n) \Leftrightarrow X(e^{j\omega } )Y(e^{j\omega } )'