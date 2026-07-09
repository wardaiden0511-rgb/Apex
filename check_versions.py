import urllib.request
import re

print('--- loom versions ---')
url = 'https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml'
with urllib.request.urlopen(url) as r:
    txt = r.read().decode('utf-8')
versions = re.findall(r'<version>([^<]+)</version>', txt)
for v in versions[-40:]:
    print(v)

print('--- yarn 1.21.11 entries ---')
url = 'https://maven.fabricmc.net/net/fabricmc/yarn/'
with urllib.request.urlopen(url) as r:
    txt = r.read().decode('utf-8')
for m in re.findall(r'>(1\.21\.11[^<]*)<', txt):
    print(m)
