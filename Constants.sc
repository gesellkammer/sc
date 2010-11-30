DoneAction {
    *nothing { ^0 }
    *pause { ^1}
    *free { ^2 }
    *freeThisAndPrevious { ^3 }
    *freeThisAndNext { ^4 }
    *freeThisAndPrevGroup { ^5 }
    *freeThisAndNextGroup { ^6 }
    *freeThisAndAllPrevNodesInGroup { ^7 }
    *freeThisAndAllNextNodesInGroup { ^8 }
    *freeThisAndPausePrev { ^9 }
    *freeThisAndPauseNext { ^10 }
    *freeNodesInGroup { ^13 }
    *freeGroup { ^14 }
}

AddAction {
    *head   { ^ \addToHead }
    *tail   { ^ \addToTail }
    *after  { ^ \addAfter  }
    *before { ^ \addBefore }
    *replace{ ^ \addReplace}
}

