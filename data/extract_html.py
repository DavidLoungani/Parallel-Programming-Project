#!/usr/bin/python
# coding=utf-8

import sys
from argparse import ArgumentParser
from bs4 import BeautifulSoup
from sets import Set


parser = ArgumentParser(description="Extract text from html files")
parser.add_argument("-i", "--input_file", nargs='*', help="html files to be processed")
parser.add_argument("-l", "--list_file", type=str, help="all html lists")
parser.add_argument("-o", "--output_directory", type=str, help="extracted txt directory")
parser.add_argument("-f", "--output_file", type=str, help="output file for mapreduce")

options = parser.parse_args()
input_list = options.input_file
list_file = options.list_file 
output_directory = options.output_directory 
output_file = options.output_file

with open(list_file, "r") as f:
    popular_list = f.read().splitlines()
    f.close()

results = {}
for title in popular_list:
    results[title] = [1.0, []]

for input_file in input_list:
    try:
        print 'Processing file: ' + input_file
        with open(input_file, "r") as f:
            html_doc = f.read()
            f.close()
            
        filename = input_file.split('/')[-1]
        if output_directory[-1] != '/':
            output_directory += '/'
        outputfile = output_directory + filename + '.txt' 
        output = open(outputfile, "w")

        soup = BeautifulSoup(html_doc, "html5lib")
        output.write(soup.select("#firstHeading")[0].get_text() + '\n')

        link_page_list = Set([]) 
        for link in soup.find_all('a'):
            url = link.get('href')
            if url == None:
                continue
            if url == '#':
                continue
            if url.find('/wiki/') != 0:
                continue
            link_page = url.split('/')[2]
            if link_page not in popular_list:
                continue
            if link_page == filename:
                continue
            link_page_list.add(link_page)
            if link_page not in results[filename][1]:
                results[filename][1].append(link_page)
            
        selects = soup.select("#bodyContent")
        for item in selects:
            text = item.get_text()
            text = text[:text.find('External links[edit]')].replace('\n', '')
            output.write(text.encode('utf-8'))
        output.write('\n')
        
    except Exception, e:
        print 'Error in processing file ' + input_file
        print e
        print sys.exc_traceback.tb_lineno 
        continue

output_file = open(output_file, "w")

for key in results:
    output_file.write(key + ' ' + str(results[key][0]) + ' ') 
    outlink = ",".join(results[key][1])
    output_file.write(outlink + '\n')

