# LaTeRm

### Terminal capable of rendering latex expressions within itself

![Alt Text](https://j.gifs.com/p8A6w2.gif)

### Powerful enaugh to handle vim

![Alt Text](https://j.gifs.com/oVz9YK.gif)

### Note

We've essentially reserved special code for latex expressions: `\u001b[Y` - when the terminal sees it,
it parses incoming characters as latex expression up to the point when we get the same code again.

It was actually a bit tricky since normal terminals assume that each line has the same height but we cannot make that assumption with latex.

As this is just an educational project there are still some bugs and things left undone:

- handling resizing
- giving possibility to change the font and color palette (althoug we support 24bit colors - which used to be pretty rare among terminals :))
- and handling the rest of console controll codes - right now we ignore things that we don't handle
  but for some programs this may lead to corrupted view (messed up foreground colors etc..)

It's remarkable that with less than 2k lines of scala code it's possible to create program essential (with few restictions though) for
every linux user.
