#!/usr/bin/env python3
"""Generate print-ready EAN-13 labels from DemoSeed.java, with no external dependencies."""
from pathlib import Path
import html
import re
ROOT = Path(__file__).resolve().parents[1]
L = ['0001101','0011001','0010011','0111101','0100011','0110001','0101111','0111011','0110111','0001011']
G = ['0100111','0110011','0011011','0100001','0011101','0111001','0000101','0010001','0001001','0010111']
PARITY = ['LLLLLL','LLGLGG','LLGGLG','LLGGGL','LGLLGG','LGGLLG','LGGGLL','LGLGLG','LGLGGL','LGGLGL']
def encode(code):
    assert re.fullmatch(r'[0-9]{13}', code)
    assert (10 - sum(int(n)*(1 if i%2==0 else 3) for i,n in enumerate(code[:12]))%10)%10 == int(code[-1])
    bits = '101' + ''.join((L if mode=='L' else G)[int(n)] for n,mode in zip(code[1:7],PARITY[int(code[0])]))
    bits += '01010' + ''.join(''.join('1' if b=='0' else '0' for b in L[int(n)]) for n in code[7:]) + '101'
    assert len(bits)==95
    return bits

def main():
    source=(ROOT/'app/src/main/java/com/example/hackathon_letu_scanner/data/DemoSeed.java').read_text()
    products=re.findall(r'new Product\("([^"]+)", "([0-9]+)", "([^"]+)"',source)
    assert len(products)==5
    products.append(('UNKNOWN','5901234123457','Неизвестный товар — проверка ошибки'))
    labels=[]
    for sku,code,name in products:
        bits=encode(code)
        # 11+7 minimum quiet zones; use 12 on both sides, no border near the barcode.
        bars=''.join(f'<rect x="{(i+12)*4}" y="48" width="4" height="{110 if i<3 or 45<=i<50 or i>=92 else 100}"/>' for i,b in enumerate(bits) if b=='1')
        svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="476" height="202" viewBox="0 0 476 202">
<rect width="476" height="202" fill="white"/>
<text x="238" y="24" text-anchor="middle" font-family="sans-serif" font-size="15">{html.escape(sku+' · '+name)}</text>
<g fill="black" shape-rendering="crispEdges">{bars}</g>
<text x="238" y="187" text-anchor="middle" font-family="monospace" font-size="23" letter-spacing="4">{code}</text>
</svg>'''
        (ROOT/f'docs/labels/{sku}.svg').write_text(svg+'\n')
        labels.append('<section>'+svg+'</section>')
    page='''<!doctype html><html lang="ru"><meta charset="utf-8"><title>ТСД · Тестовые EAN-13</title>
<style>body{font:16px sans-serif;background:white;color:black;margin:20px}h1{font-size:24px}section{break-inside:avoid;margin:20px 0}svg{max-width:100%;height:auto;width:126mm}@media print{@page{size:A4;margin:12mm}h1,p{display:none}section{margin:0 0 14mm}section:nth-of-type(3n){break-after:page}}</style>
<h1>Тестовые этикетки ТСД</h1><p>Печатайте в масштабе 100%. Демо: GEL-004 → GEL-004 → PERF-001 → CRM-002. UNKNOWN проверяет ошибку каталога.</p>'''+''.join(labels)+'</html>\n'
    (ROOT/'docs/demo-labels.html').write_text(page)
    print(f'Generated {len(products)} valid EAN-13 labels and docs/demo-labels.html')

if __name__=='__main__': main()
