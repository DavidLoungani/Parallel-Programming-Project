#!/usr/bin/python

import re
import sys

if len(sys.argv) != 2:
    print 'argument error'
    sys.exit(2)

inputfile = sys.argv[1]

with open(inputfile, "r") as f:
    data = f.read().replace('\n', '')

p = '<td><a href="\/wiki\/([^"]*)" title='

titles = re.findall(p, data)

for title in titles:
    print title.strip()
