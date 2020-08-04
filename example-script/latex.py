def printDelimiter():
    print("\u001b[92m")
    print("-" * 64)
    print("\u001b[0m", end = '')

while True:
    printDelimiter()
    latex = input("Hand me some LaTeX -> ")
    print()
    print(f"There You go! \u001b[93m\u001b[Y{latex}\u001b[Y\u001b[0m")


