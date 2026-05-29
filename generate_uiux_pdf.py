#!/usr/bin/env python3
"""
UI/UX 디자인 후보군 PDF - 레이아웃 3종
A: 클래식 월간 캘린더 + FAB
B: 위클리 스트립 + 대형 카드 + 바텀시트
C: 대시보드 + 단계별 마법사 입력
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib.colors import HexColor
from reportlab.pdfgen import canvas as pdfmod
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

try:
    pdfmetrics.registerFont(TTFont('KR',  'C:/Windows/Fonts/malgun.ttf'))
    pdfmetrics.registerFont(TTFont('KRB', 'C:/Windows/Fonts/malgunbd.ttf'))
    KR, KRB = 'KR', 'KRB'
except:
    KR, KRB = 'Helvetica', 'Helvetica-Bold'

W, H = A4
OUTPUT = 'C:/Users/김정현/IdeaProjects/docs/uiux-candidates.pdf'

PW = 165   # phone screen interior width
PH = 355   # phone screen interior height
FR = 8     # frame thickness

# ── palette per design ──────────────────────────────────────────────────────
DA = dict(
    pri='#1976D2', pri_d='#1565C0', pri_l='#E3F2FD',
    acc='#FF5722', bg='#FFFFFF', surf='#F4F7FC',
    txt='#1A1A1A', txt2='#757575', sep='#E0E0E0',
    sta='#1565C0', card='#FFFFFF', btn='#1976D2', btn_t='#FFFFFF',
    h_bg='#1976D2', h_t='#FFFFFF',
    dot=['#F44336','#FF9800','#4CAF50','#9C27B0'],
    in_bg='#F5F5F5', in_bd='#BDBDBD',
    tab='#F4F7FC', tab_a='#1976D2',
    name='클래식 캘린더형',
    desc='전통적인 월간 캘린더\n날짜 클릭 → 하단 패널\nFAB 버튼으로 빠른 일정 추가\n전체 화면 폼 입력',
)
DB = dict(
    pri='#00897B', pri_d='#00695C', pri_l='#E0F2F1',
    acc='#FF6D00', bg='#FAFAFA', surf='#FFFFFF',
    txt='#212121', txt2='#757575', sep='#E0E0E0',
    sta='#00695C', card='#FFFFFF', btn='#00897B', btn_t='#FFFFFF',
    h_bg='#FAFAFA', h_t='#212121',
    dot=['#E53935','#F57C00','#388E3C','#7B1FA2'],
    in_bg='#F5F5F5', in_bd='#BDBDBD',
    tab='#FAFAFA', tab_a='#00897B',
    name='위클리 스트립형',
    desc='상단: 7일 스트립 (가로 스크롤)\n선택 날짜 → 대형 카드 리스트\n하단 탭바 네비게이션\n바텀시트 팝업 폼 입력',
)
DC = dict(
    pri='#6A1B9A', pri_d='#4A148C', pri_l='#F3E5F5',
    acc='#F9A825', bg='#F8F5FC', surf='#FFFFFF',
    txt='#1A1A1A', txt2='#757575', sep='#E8DEF8',
    sta='#4A148C', card='#FFFFFF', btn='#6A1B9A', btn_t='#FFFFFF',
    h_bg='#6A1B9A', h_t='#FFFFFF',
    dot=['#E53935','#F57C00','#2E7D32','#1565C0'],
    in_bg='#F3E5F5', in_bd='#CE93D8',
    tab='#EDE7F6', tab_a='#6A1B9A',
    name='대시보드 + 마법사형',
    desc='상단: 인사말 + 미니 캘린더 + 통계\n카테고리 필터 탭\n세로 이벤트 피드\n단계별 마법사 폼 (1→2→3)',
)

DESIGNS = [
    dict(id='A', d=DA,
         scr_label=['메인 화면 (월간 캘린더)', '일정 등록 (전체 화면 폼)'],
         ui=['클래식 월간 캘린더', 'FAB 버튼으로 빠른 추가', '하단 슬라이딩 패널', '전체 화면 순차 폼'],
         page_bg='#EEF4FF'),
    dict(id='B', d=DB,
         scr_label=['메인 화면 (위클리 스트립)', '일정 등록 (바텀 시트)'],
         ui=['상단 7일 위클리 스트립', '대형 이벤트 카드', '하단 탭바 네비게이션', '바텀시트 슬라이드업 폼'],
         page_bg='#E8F5F3'),
    dict(id='C', d=DC,
         scr_label=['메인 화면 (대시보드)', '일정 등록 (단계별 마법사)'],
         ui=['인사말 + 미니 캘린더 + 통계', '카테고리 필터 탭', '이벤트 피드 스크롤', '카테고리 선택 → 날짜 → 상세'],
         page_bg='#F3EEF8'),
]

# ── drawing helpers ──────────────────────────────────────────────────────────

def rr(c, x, y, w, h, r, fill=None, stroke=None, sw=0.8):
    if fill:
        c.setFillColor(HexColor(fill))
    if stroke:
        c.setStrokeColor(HexColor(stroke))
        c.setLineWidth(sw)
    c.roundRect(x, y, w, h, r, fill=1 if fill else 0, stroke=1 if stroke else 0)

def t(c, s, x, y, sz=8, bold=False, col='#111111', align='left'):
    c.setFont(KRB if bold else KR, sz)
    c.setFillColor(HexColor(col))
    if align == 'center': c.drawCentredString(x, y, s)
    elif align == 'right': c.drawRightString(x, y, s)
    else: c.drawString(x, y, s)

def ln(c, x1, y1, x2, y2, col='#CCCCCC', w=0.6):
    c.setStrokeColor(HexColor(col)); c.setLineWidth(w)
    c.line(x1, y1, x2, y2)

def phone_frame(c, sx, sy):
    fw, fh = PW+FR*2, PH+FR*2
    fx, fy = sx-FR, sy-FR
    c.setFillColor(HexColor('#00000022')); c.roundRect(fx+4,fy-5,fw,fh,22,fill=1,stroke=0)
    c.setFillColor(HexColor('#1A1A1A'));   c.roundRect(fx,fy,fw,fh,22,fill=1,stroke=0)
    c.setFillColor(HexColor('#2C2C2C'))
    c.rect(fx+fw, fy+fh-110, 3, 36, fill=1, stroke=0)
    c.rect(fx-3, fy+fh-95,   3, 26, fill=1, stroke=0)
    c.rect(fx-3, fy+fh-132,  3, 26, fill=1, stroke=0)

def status_bar(c, sx, sy, d):
    rr(c, sx, sy, PW, PH, 16, fill=d['bg'])
    rr(c, sx, sy+PH-22, PW, 22, 16, fill=d['sta'])
    c.setFillColor(HexColor(d['sta'])); c.rect(sx, sy+PH-22, PW, 11, fill=1, stroke=0)
    t(c, '9:41', sx+8, sy+PH-15, sz=7, bold=True, col='#FFFFFF')
    # battery
    bx,by = sx+PW-22, sy+PH-16
    c.setStrokeColor(HexColor('#FFFFFFCC')); c.setLineWidth(0.7)
    c.rect(bx,by,14,7,fill=0,stroke=1)
    c.setFillColor(HexColor('#FFFFFFCC')); c.rect(bx+14,by+2,2,3,fill=1,stroke=0)
    c.rect(bx+1,by+1,9,5,fill=1,stroke=0)
    # home indicator
    rr(c, sx+PW//2-22, sy+4, 44, 3, 2, fill='#00000028')

# ────────────────────────────────────────────────────────────────────────────
#  DESIGN A — 클래식 캘린더형
# ────────────────────────────────────────────────────────────────────────────

CAL = [
    [None,1,2,3,4,5,6],
    [7,8,9,10,11,12,13],
    [14,15,16,17,18,19,20],
    [21,22,23,24,25,26,27],
    [28,29,30,None,None,None,None],
]
DOTS = {8:0, 10:1, 15:0, 22:2, 26:1}  # date → dot color index
TODAY, SEL = 15, 8

def draw_A_main(c, sx, sy, d):
    phone_frame(c, sx, sy); status_bar(c, sx, sy, d)
    top = sy+PH-22

    # header
    top -= 32
    c.setFillColor(HexColor(d['h_bg'])); c.rect(sx,top,PW,32,fill=1,stroke=0)
    t(c,'‹',sx+12,top+10,sz=13,col=d['h_t'])
    t(c,'2026년 6월',sx+PW//2,top+11,sz=10,bold=True,col=d['h_t'],align='center')
    t(c,'›',sx+PW-12,top+10,sz=13,col=d['h_t'],align='right')

    # dow row
    top -= 16
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,top,PW,16,fill=1,stroke=0)
    cw = PW/7
    for i,day in enumerate(['일','월','화','수','목','금','토']):
        cx = sx+i*cw+cw/2
        col = '#D32F2F' if i==0 else '#1565C0' if i==6 else d['txt2']
        t(c,day,cx,top+4,sz=7,col=col,align='center')
    ln(c,sx,top,sx+PW,top,col=d['sep'])

    # calendar grid
    rh = 18
    for ri,week in enumerate(CAL):
        for ci,day in enumerate(week):
            if day is None: continue
            cx = sx+ci*cw+cw/2
            cy_c = top-ri*rh-rh/2
            if day == TODAY:
                c.setFillColor(HexColor(d['pri'])); c.circle(cx,cy_c+2,8,fill=1,stroke=0)
                t(c,str(day),cx,cy_c-2,sz=7.5,bold=True,col='#FFFFFF',align='center')
            elif day == SEL:
                c.setFillColor(HexColor(d['pri_l'])); c.circle(cx,cy_c+2,8,fill=1,stroke=0)
                t(c,str(day),cx,cy_c-2,sz=7.5,bold=True,col=d['pri_d'],align='center')
            else:
                tc = '#D32F2F' if ci==0 else '#1565C0' if ci==6 else d['txt']
                t(c,str(day),cx,cy_c-2,sz=7.5,col=tc,align='center')
            if day in DOTS:
                c.setFillColor(HexColor(d['dot'][DOTS[day]]))
                c.circle(cx,cy_c-7,1.7,fill=1,stroke=0)
    grid_bot = top-5*rh

    # bottom panel
    ln(c,sx,grid_bot,sx+PW,grid_bot,col=d['sep'],w=1)
    panel_h = grid_bot-(sy+12)
    c.setFillColor(HexColor(d['surf'])); c.rect(sx,sy+12,PW,panel_h,fill=1,stroke=0)

    t(c,'6월 8일 (월)',sx+10,grid_bot-13,sz=8.5,bold=True,col=d['txt'])

    cards = [(d['dot'][0],'야구 · 홈','두산 vs LG'),(d['dot'][1],'농구','국내 농구 직관')]
    cy2 = grid_bot-28
    for dot_c,badge,title in cards:
        if cy2<sy+42: break
        c.setFillColor(HexColor('#00000010')); c.roundRect(sx+9,cy2-1,PW-16,22,4,fill=1,stroke=0)
        rr(c,sx+8,cy2,PW-16,22,4,fill=d['card'])
        rr(c,sx+8,cy2,3,22,2,fill=dot_c)
        rr(c,sx+16,cy2+13,32,7,2,fill=dot_c+'28')
        t(c,badge,sx+32,cy2+15,sz=5.5,col=dot_c,align='center')
        t(c,title,sx+16,cy2+4,sz=7,col=d['txt'])
        cy2 -= 26

    # FAB
    fab_x, fab_y = sx+PW-28, sy+18
    c.setFillColor(HexColor(d['acc']))
    c.circle(fab_x,fab_y+9,13,fill=1,stroke=0)
    c.setFillColor(HexColor('#FFFFFF')); c.setLineWidth(1.5)
    c.line(fab_x-6,fab_y+9,fab_x+6,fab_y+9); c.line(fab_x,fab_y+3,fab_x,fab_y+15)

def draw_A_form(c, sx, sy, d):
    phone_frame(c, sx, sy); status_bar(c, sx, sy, d)
    top = sy+PH-22

    # header
    top -= 32
    c.setFillColor(HexColor(d['h_bg'])); c.rect(sx,top,PW,32,fill=1,stroke=0)
    t(c,'‹',sx+10,top+10,sz=12,col=d['h_t'])
    t(c,'일정 등록',sx+PW//2,top+11,sz=10,bold=True,col=d['h_t'],align='center')

    c.setFillColor(HexColor(d['bg'])); c.rect(sx,sy,PW,top-sy,fill=1,stroke=0)

    cy = top-10

    def lbl(text):
        nonlocal cy
        cy -= 10; t(c,text,sx+10,cy,sz=6.5,col=d['txt2'])
        cy -= 3

    def inp(val,h=18):
        nonlocal cy
        cy -= h
        rr(c,sx+8,cy,PW-16,h,4,fill=d['in_bg'],stroke=d['in_bd'],sw=0.8)
        t(c,val,sx+13,cy+5,sz=7,col=d['txt'])
        cy -= 5

    lbl('날짜'); inp('2026 - 06 - 08   📅')
    lbl('제목'); inp('두산 vs LG')

    lbl('카테고리')
    cy -= 2
    cats = ['야구','농구','축구','여자배구','남자배구','기타']
    catw = (PW-16)/3
    for i,cat in enumerate(cats):
        r2,c2 = divmod(i,3)
        rx=sx+8+c2*catw; ry=cy-r2*14-13
        sel=(cat=='야구')
        rr(c,rx+1,ry,catw-2,12,3,fill=d['pri_l'] if sel else d['in_bg'],stroke=d['pri'] if sel else d['in_bd'],sw=0.8)
        c.setStrokeColor(HexColor(d['pri'])); c.setLineWidth(0.8)
        c.circle(rx+7,ry+6,3,fill=0,stroke=1)
        if sel:
            c.setFillColor(HexColor(d['pri'])); c.circle(rx+7,ry+6,1.5,fill=1,stroke=0)
        t(c,cat,rx+13,ry+3,sz=6,bold=sel,col=d['pri'] if sel else d['txt'])
    cy -= 2*14+8

    lbl('홈 / 원정')
    cy -= 2
    for i,lbl2 in enumerate(['홈','원정']):
        bw=(PW-20)/2; bx=sx+8+i*(bw+4)
        sel=(lbl2=='홈')
        rr(c,bx,cy-13,bw,13,3,fill=d['pri_l'] if sel else d['in_bg'],stroke=d['pri'] if sel else d['in_bd'],sw=0.8)
        c.setStrokeColor(HexColor(d['pri'])); c.setLineWidth(0.8)
        c.circle(bx+7,cy-6.5,3,fill=0,stroke=1)
        if sel:
            c.setFillColor(HexColor(d['pri'])); c.circle(bx+7,cy-6.5,1.5,fill=1,stroke=0)
        t(c,lbl2,bx+13,cy-11,sz=7,bold=sel,col=d['pri'] if sel else d['txt'])
    cy -= 22

    lbl('메모 (선택)')
    memo_h = min(42, cy-sy-34)
    cy -= memo_h
    rr(c,sx+8,cy,PW-16,memo_h,4,fill=d['in_bg'],stroke=d['in_bd'],sw=0.8)
    t(c,'메모를 입력하세요',sx+13,cy+memo_h-13,sz=7,col=d['txt2']+'99')

    rr(c,sx+8,sy+16,PW-16,18,9,fill=d['btn'])
    t(c,'저장',sx+PW//2,sy+22,sz=8,bold=True,col=d['btn_t'],align='center')

# ────────────────────────────────────────────────────────────────────────────
#  DESIGN B — 위클리 스트립형
# ────────────────────────────────────────────────────────────────────────────

def draw_B_main(c, sx, sy, d):
    phone_frame(c, sx, sy); status_bar(c, sx, sy, d)
    top = sy+PH-22

    # ── top header (no color, minimal)
    top -= 22
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,top,PW,22,fill=1,stroke=0)
    t(c,'6월 2026',sx+12,top+7,sz=10,bold=True,col=d['txt'])
    t(c,'검색',sx+PW-30,top+7,sz=8,col=d['pri'])
    t(c,'⋮',sx+PW-12,top+7,sz=11,col=d['txt2'])

    # ── weekly strip
    top -= 46
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,top,PW,46,fill=1,stroke=0)
    week_days = [('5','일'),('6','월'),('7','화'),('8','수'),('9','목'),('10','금'),('11','토')]
    # "8" = selected
    ww = PW/7
    for i,(num,dow) in enumerate(week_days):
        cx = sx+i*ww+ww/2
        sel = (num=='8')
        tc_dow = '#D32F2F' if dow=='일' else '#388E3C' if dow=='토' else d['txt2']
        t(c,dow,cx,top+35,sz=7,col=tc_dow,align='center')
        if sel:
            c.setFillColor(HexColor(d['pri'])); c.circle(cx,top+20,13,fill=1,stroke=0)
            t(c,num,cx,top+16,sz=9,bold=True,col='#FFFFFF',align='center')
        else:
            if num=='15':  # today dot
                c.setFillColor(HexColor(d['acc'])); c.circle(cx,top+8,2,fill=1,stroke=0)
            t(c,num,cx,top+16,sz=9,col=d['txt'],align='center')

    ln(c,sx,top,sx+PW,top,col=d['sep'],w=1)

    # ── date label row
    top -= 22
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,top,PW,22,fill=1,stroke=0)
    t(c,'6월 8일 수요일',sx+12,top+7,sz=8.5,bold=True,col=d['txt'])
    t(c,'+ 추가',sx+PW-10,top+7,sz=8,col=d['pri'],align='right')

    # ── event cards (large)
    evts = [
        (d['dot'][0],'야구','홈','두산 vs LG','2026.06.08'),
        (d['dot'][1],'농구','—','국내 농구 직관','2026.06.08'),
    ]
    card_h = 58
    gap = 8
    cy2 = top-gap
    for dot_c,cat,sub,title,date in evts:
        cy2 -= card_h
        if cy2 < sy+48: break
        # shadow
        c.setFillColor(HexColor('#00000012')); c.roundRect(sx+9,cy2-2,PW-16,card_h,8,fill=1,stroke=0)
        rr(c,sx+8,cy2,PW-16,card_h,8,fill=d['card'])
        # colored left border
        rr(c,sx+8,cy2,4,card_h,2,fill=dot_c)
        # category tag
        rr(c,sx+18,cy2+card_h-18,32,13,4,fill=dot_c+'22')
        t(c,cat,sx+34,cy2+card_h-12,sz=6.5,col=dot_c,align='center')
        if sub!='—':
            rr(c,sx+54,cy2+card_h-18,28,13,4,fill=dot_c+'15')
            t(c,sub,sx+68,cy2+card_h-12,sz=6.5,col=dot_c,align='center')
        # title
        t(c,title,sx+18,cy2+card_h-32,sz=9.5,bold=True,col=d['txt'])
        t(c,date,sx+18,cy2+card_h-44,sz=6.5,col=d['txt2'])
        # chevron
        t(c,'›',sx+PW-20,cy2+card_h//2-4,sz=12,col=d['txt2'])
        cy2 -= gap

    # ── bottom tab bar
    tab_h = 42
    rr(c,sx,sy+12,PW,tab_h,0,fill=d['bg'])
    ln(c,sx,sy+12+tab_h,sx+PW,sy+12+tab_h,col=d['sep'],w=0.8)
    tabs = [('홈','●'),('캘린더','○'),('설정','○')]
    tw = PW/3
    for i,(label,dot2) in enumerate(tabs):
        tcx = sx+i*tw+tw/2
        is_a = (label=='홈')
        tc = d['pri'] if is_a else d['txt2']
        # icon placeholder circle
        c.setStrokeColor(HexColor(tc)); c.setFillColor(HexColor(tc if is_a else '#00000000'))
        c.setLineWidth(1); c.circle(tcx,sy+12+28,8,fill=1 if is_a else 0,stroke=1)
        t(c,label,tcx,sy+12+9,sz=6.5,col=tc,align='center')

def draw_B_form(c, sx, sy, d):
    """Bottom sheet over blurred calendar"""
    phone_frame(c, sx, sy)
    # background: draw muted calendar
    c.setFillColor(HexColor(d['bg'])); c.roundRect(sx,sy,PW,PH,16,fill=1,stroke=0)
    # status bar
    rr(c,sx,sy+PH-22,PW,22,16,fill=d['sta'])
    c.setFillColor(HexColor(d['sta'])); c.rect(sx,sy+PH-22,PW,11,fill=1,stroke=0)
    t(c,'9:41',sx+8,sy+PH-15,sz=7,bold=True,col='#FFFFFF')

    # muted calendar header
    mh = sy+PH-22-28
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,mh,PW,28,fill=1,stroke=0)
    t(c,'2026년 6월',sx+PW//2,mh+9,sz=9,bold=True,col=d['txt']+'55',align='center')

    # muted week strip
    wstrip_y = mh-40
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,wstrip_y,PW,40,fill=1,stroke=0)
    ww = PW/7
    for i,num in enumerate(['5','6','7','8','9','10','11']):
        cx = sx+i*ww+ww/2
        t(c,num,cx,wstrip_y+12,sz=8,col=d['txt']+'30',align='center')

    # dim overlay
    c.setFillColor(HexColor('#00000040'))
    c.rect(sx,sy,PW,wstrip_y-sy,fill=1,stroke=0)
    c.setFillColor(HexColor('#00000015'))
    c.rect(sx,wstrip_y,PW,mh-wstrip_y+28,fill=1,stroke=0)

    # ── bottom sheet ──
    sheet_h = 220
    sheet_y = sy+10
    rr(c,sx,sheet_y,PW,sheet_h,16,fill=d['card'])
    # drag handle
    rr(c,sx+PW//2-18,sheet_y+sheet_h-10,36,4,2,fill=d['sep'])

    # sheet header
    t(c,'일정 추가',sx+12,sheet_y+sheet_h-22,sz=10,bold=True,col=d['txt'])
    t(c,'✕',sx+PW-16,sheet_y+sheet_h-22,sz=10,col=d['txt2'])

    # date pill
    date_y = sheet_y+sheet_h-40
    rr(c,sx+12,date_y,60,16,8,fill=d['pri_l'])
    t(c,'6월 8일 (수)',sx+42,date_y+4,sz=7,col=d['pri'],align='center')

    # category chips (horizontal)
    chip_y = sheet_y+sheet_h-62
    chips = [('야구',True),('+농구',False),('+축구',False),('+기타',False)]
    cx2 = sx+12
    for label,sel in chips:
        cw2=len(label)*7+10
        rr(c,cx2,chip_y,cw2,16,8,
           fill=d['pri'] if sel else d['in_bg'],
           stroke=None if sel else d['in_bd'],sw=0.6)
        t(c,label,cx2+cw2//2,chip_y+4,sz=6.5,
          col='#FFFFFF' if sel else d['txt2'],align='center')
        cx2+=cw2+5

    # baseball type
    bt_y = chip_y-22
    t(c,'홈 / 원정',sx+12,bt_y+10,sz=6.5,col=d['txt2'])
    for i,lbl in enumerate(['홈','원정']):
        bx=sx+12+i*45; sel=(lbl=='홈')
        rr(c,bx,bt_y-4,38,14,7,
           fill=d['pri'] if sel else d['in_bg'],
           stroke=None if sel else d['in_bd'],sw=0.6)
        t(c,lbl,bx+19,bt_y+1,sz=7,col='#FFFFFF' if sel else d['txt2'],align='center')

    # title input
    title_y = bt_y-24
    rr(c,sx+12,title_y,PW-24,18,4,fill=d['in_bg'],stroke=d['in_bd'],sw=0.8)
    t(c,'두산 vs LG',sx+18,title_y+5,sz=7.5,col=d['txt'])

    # memo input
    memo_y = title_y-26
    rr(c,sx+12,memo_y,PW-24,20,4,fill=d['in_bg'],stroke=d['in_bd'],sw=0.8)
    t(c,'메모 (선택)',sx+18,memo_y+6,sz=7,col=d['txt2']+'88')

    # save button
    rr(c,sx+12,sheet_y+16,PW-24,22,11,fill=d['btn'])
    t(c,'일정 저장',sx+PW//2,sheet_y+23,sz=8.5,bold=True,col=d['btn_t'],align='center')

# ────────────────────────────────────────────────────────────────────────────
#  DESIGN C — 대시보드 + 마법사형
# ────────────────────────────────────────────────────────────────────────────

def draw_C_main(c, sx, sy, d):
    phone_frame(c, sx, sy); status_bar(c, sx, sy, d)
    top = sy+PH-22

    # ── greeting header
    top -= 40
    c.setFillColor(HexColor(d['h_bg'])); c.rect(sx,top,PW,40,fill=1,stroke=0)
    t(c,'안녕하세요, 홍길동 👋',sx+12,top+22,sz=9,bold=True,col='#FFFFFF')
    t(c,'오늘 일정을 확인하세요',sx+12,top+10,sz=7,col='#FFFFFFAA')

    # ── mini-cal + stats row
    top -= 82
    c.setFillColor(HexColor(d['surf'])); c.rect(sx,top,PW,82,fill=1,stroke=0)

    # mini calendar (left half)
    mcx = sx+8; mcy = top+6; mcw = PW//2-12; mch = 70
    rr(c,mcx,mcy,mcw,mch,6,fill=d['bg'],stroke=d['sep'],sw=0.6)
    t(c,'6월',mcx+mcw//2,mcy+mch-10,sz=7,bold=True,col=d['pri'],align='center')
    mini_days=['일','월','화','수','목','금','토']
    mini_cw=mcw/7
    for i,day in enumerate(mini_days):
        cx3=mcx+i*mini_cw+mini_cw/2
        t(c,day,cx3,mcy+mch-20,sz=5,col='#D32F2F' if i==0 else '#888888',align='center')
    # just 2 rows of dates (small)
    mini_cal=[[None,1,2,3,4,5,6],[7,8,9,10,11,12,13]]
    for ri,week in enumerate(mini_cal):
        for ci,day in enumerate(week):
            if day is None: continue
            cx3=mcx+ci*mini_cw+mini_cw/2
            cy3=mcy+mch-30-ri*12
            if day==8:
                c.setFillColor(HexColor(d['pri'])); c.circle(cx3,cy3+3,5,fill=1,stroke=0)
                t(c,str(day),cx3,cy3,sz=5,bold=True,col='#FFFFFF',align='center')
            else:
                t(c,str(day),cx3,cy3,sz=5,col=d['txt'],align='center')
    t(c,'…',mcx+mcw//2,mcy+10,sz=8,col=d['txt2'],align='center')

    # stats (right half)
    stx=sx+PW//2+4; sty=mcy; stw=PW//2-12; sth=mch
    rr(c,stx,sty,stw,sth,6,fill=d['bg'],stroke=d['sep'],sw=0.6)
    t(c,'이번 달',stx+stw//2,sty+sth-10,sz=7,bold=True,col=d['pri'],align='center')
    stats=[('⚾ 야구','3회',d['dot'][0]),('🏀 농구','1회',d['dot'][1]),('⚽ 기타','1회',d['dot'][2])]
    for si,(icon,val,sc) in enumerate(stats):
        sy3=sty+sth-24-si*15
        t(c,icon,stx+8,sy3,sz=6.5,col=d['txt'])
        t(c,val,stx+stw-8,sy3,sz=6.5,bold=True,col=sc,align='right')

    # ── category filter tabs
    top -= 28
    c.setFillColor(HexColor(d['bg'])); c.rect(sx,top,PW,28,fill=1,stroke=0)
    ln(c,sx,top+28,sx+PW,top+28,col=d['sep'])
    tabs=['전체','야구','농구','축구']
    tx=sx+8
    for i,tab in enumerate(tabs):
        tw2=len(tab)*8+14; is_a=(i==0)
        if is_a:
            rr(c,tx,top+6,tw2,16,8,fill=d['pri'])
            t(c,tab,tx+tw2//2,top+10,sz=7,bold=True,col='#FFFFFF',align='center')
        else:
            rr(c,tx,top+6,tw2,16,8,fill=d['in_bg'])
            t(c,tab,tx+tw2//2,top+10,sz=7,col=d['txt2'],align='center')
        tx+=tw2+6

    # ── event feed (big colorful cards)
    evts=[
        (d['dot'][0],'⚾ 야구','홈 경기','두산 vs LG','6월 8일'),
        (d['dot'][2],'⚽ 축구','—','K리그 직관','6월 10일'),
        (d['dot'][0],'⚾ 야구','원정','두산 @ 잠실','6월 15일 ●오늘'),
    ]
    cy4=top-8
    for dot_c,icon,sub,title,date in evts:
        ch=46
        cy4-=ch
        if cy4<sy+14: break
        rr(c,sx+8,cy4,PW-16,ch,8,fill=dot_c+'18')
        c.setStrokeColor(HexColor(dot_c+'60')); c.setLineWidth(0.8)
        c.roundRect(sx+8,cy4,PW-16,ch,8,fill=0,stroke=1)
        # icon circle
        c.setFillColor(HexColor(dot_c)); c.circle(sx+24,cy4+ch//2,12,fill=1,stroke=0)
        t(c,icon[:2],sx+24,cy4+ch//2-4,sz=8,col='#FFFFFF',align='center')
        # text
        t(c,title,sx+42,cy4+ch-14,sz=8.5,bold=True,col=d['txt'])
        tag=sub if sub!='—' else ''
        if tag:
            rr(c,sx+42,cy4+6,len(tag)*7+8,12,4,fill=dot_c+'25')
            t(c,tag,sx+42+4,cy4+9,sz=6,col=dot_c)
            t(c,date,sx+42+len(tag)*7+18,cy4+9,sz=6,col=d['txt2'])
        else:
            t(c,date,sx+42,cy4+9,sz=6,col=d['txt2'])
        t(c,'›',sx+PW-18,cy4+ch//2-4,sz=11,col=dot_c)
        cy4-=6

def draw_C_form(c, sx, sy, d):
    """Step-by-step wizard: step 1 = category selection"""
    phone_frame(c, sx, sy); status_bar(c, sx, sy, d)
    top = sy+PH-22

    # header
    top -= 32
    c.setFillColor(HexColor(d['h_bg'])); c.rect(sx,top,PW,32,fill=1,stroke=0)
    t(c,'‹',sx+10,top+10,sz=12,col=d['h_t'])
    t(c,'일정 추가',sx+PW//2,top+11,sz=10,bold=True,col=d['h_t'],align='center')
    # step indicator
    for si in range(3):
        cx5=sx+PW-40+si*10
        c.setFillColor(HexColor('#FFFFFF' if si==0 else '#FFFFFF55'))
        c.circle(cx5,top+16,3,fill=1,stroke=0)

    c.setFillColor(HexColor(d['bg'])); c.rect(sx,sy,PW,top-sy,fill=1,stroke=0)

    cy=top-18
    t(c,'어떤 종목인가요?',sx+PW//2,cy,sz=11,bold=True,col=d['txt'],align='center')
    cy-=8
    t(c,'일정에 맞는 종목을 선택하세요',sx+PW//2,cy,sz=7.5,col=d['txt2'],align='center')
    cy-=16

    # 2x3 category tile grid
    cat_tiles=[
        ('⚾','야구',d['dot'][0]),
        ('🏀','농구',d['dot'][1]),
        ('⚽','축구',d['dot'][2]),
        ('🏐','여자배구',d['dot'][3]),
        ('🏐','남자배구',d['dot'][0]),
        ('✦','기타','#9E9E9E'),
    ]
    tile_w=(PW-28)/3; tile_h=54; tile_gap=6
    for i,(icon,name2,tc) in enumerate(cat_tiles):
        row2=i//3; col2=i%3
        tx2=sx+8+col2*(tile_w+tile_gap)
        ty2=cy-row2*(tile_h+tile_gap)-tile_h
        sel=(name2=='야구')
        if sel:
            rr(c,tx2,ty2,tile_w,tile_h,10,fill=tc+'20',stroke=tc,sw=1.5)
        else:
            rr(c,tx2,ty2,tile_w,tile_h,10,fill=d['surf'],stroke=d['sep'],sw=0.8)
        # icon
        t(c,icon,tx2+tile_w/2,ty2+tile_h-18,sz=18,col=tc,align='center')
        t(c,name2,tx2+tile_w/2,ty2+8,sz=7,bold=sel,col=tc if sel else d['txt'],align='center')
        if sel:
            # check badge
            c.setFillColor(HexColor(tc)); c.circle(tx2+tile_w-8,ty2+tile_h-8,7,fill=1,stroke=0)
            t(c,'✓',tx2+tile_w-8,ty2+tile_h-12,sz=7,bold=True,col='#FFFFFF',align='center')

    cy-=2*(tile_h+tile_gap)+8

    # next button
    rr(c,sx+8,sy+16,PW-16,22,11,fill=d['btn'])
    t(c,'다음  →',sx+PW//2,sy+23,sz=9,bold=True,col=d['btn_t'],align='center')
    t(c,'1 / 3 단계',sx+PW//2,sy+10,sz=6.5,col=d['txt2'],align='center')

# ────────────────────────────────────────────────────────────────────────────
#  Page layout
# ────────────────────────────────────────────────────────────────────────────

DRAW_FUNCS = {
    'A': (draw_A_main, draw_A_form),
    'B': (draw_B_main, draw_B_form),
    'C': (draw_C_main, draw_C_form),
}

def draw_design_page(c, info):
    did = info['id']
    d   = info['d']
    labels  = info['scr_label']
    ui_pts  = info['ui']
    pg_bg   = info['page_bg']

    c.setFillColor(HexColor(pg_bg)); c.rect(0,0,W,H,fill=1,stroke=0)

    # top banner
    c.setFillColor(HexColor(d['pri'])); c.rect(0,H-66,W,66,fill=1,stroke=0)
    rr(c,28,H-52,38,22,11,fill='#FFFFFF30')
    t(c,'안 '+did,28+19,H-45,sz=10,bold=True,col='#FFFFFF',align='center')
    t(c,d['name'],76,H-43,sz=15,bold=True,col='#FFFFFF')
    t(c,d['desc'].split('\n')[0],76,H-58,sz=8,col='#FFFFFFAA')

    # phone positions
    ext_w=PW+FR*2; gap=26
    total=2*ext_w+gap
    start_x=(W-total)/2
    ph_sy=298

    fn_main, fn_form = DRAW_FUNCS[did]
    fn_main(c, start_x+FR, ph_sy, d)
    fn_form(c, start_x+ext_w+gap+FR, ph_sy, d)

    lbl_y = ph_sy-FR-15
    for i,lbl in enumerate(labels):
        px=start_x+FR+PW//2 if i==0 else start_x+ext_w+gap+FR+PW//2
        t(c,lbl,px,lbl_y,sz=8,bold=True,col=d['pri'],align='center')

    # info area
    info_top = lbl_y-12
    ln(c,36,info_top,W-36,info_top,col=d['pri']+'60',w=0.8)

    # UI 특징
    t(c,'UI 구성 특징',36,info_top-18,sz=8,bold=True,col='#333333')
    for i,pt in enumerate(ui_pts):
        t(c,'• '+pt,36,info_top-33-i*14,sz=8,col='#444444')

    # palette
    t(c,'컬러',W//2+20,info_top-18,sz=8,bold=True,col='#333333')
    pal=[d['pri'],d['pri_l'],d['acc'],d['bg'],d['txt2'],d['txt']]
    pal_lbl=['Primary','Pri Light','Accent','BG','Text Sec','Text']
    for i,(pc,pl) in enumerate(zip(pal,pal_lbl)):
        px2=W//2+20+i*(23+6)
        rr(c,px2,info_top-52,23,23,5,fill=pc,stroke='#AAAAAA44',sw=0.4)
        t(c,pl,px2+11,info_top-62,sz=5.5,col='#555555',align='center')

    ln(c,36,28,W-36,28,col=d['pri']+'40',w=0.5)
    t(c,f'개인 스케줄 관리 웹앱  ·  UI/UX 디자인 후보 {did}  ·  {d["name"]}',
      W//2,16,sz=7,col='#999999',align='center')

# ────────────────────────────────────────────────────────────────────────────
#  Title Page
# ────────────────────────────────────────────────────────────────────────────

def draw_title(c):
    c.setFillColor(HexColor('#08111C')); c.rect(0,0,W,H,fill=1,stroke=0)
    for col,cx,cy,r in [
        ('#1976D228',W*.78,H*.70,150),('#00897B22',W*.18,H*.32,110),
        ('#6A1B9A22',W*.55,H*.14,85), ('#1976D215',W*.08,H*.84,70),
    ]:
        c.setFillColor(HexColor(col)); c.circle(cx,cy,r,fill=1,stroke=0)

    t(c,'개인 스케줄 관리 웹앱',W//2,H*.73,sz=28,bold=True,col='#FFFFFF',align='center')
    t(c,'UI / UX 레이아웃 후보군  — 3종',W//2,H*.668,sz=14,col='#88AACC',align='center')
    ln(c,W//2-130,H*.637,W//2+130,H*.637,col='#FFFFFF20',w=1)

    cards = [
        ('A','#1976D2','클래식 캘린더형','월간 캘린더 + FAB + 전체화면 폼','메인: 월간 그리드','입력: 세로 스크롤 폼'),
        ('B','#00897B','위클리 스트립형','위클리 헤더 + 카드 리스트 + 바텀시트','메인: 주간 날짜 스트립','입력: 바텀시트 슬라이드업'),
        ('C','#6A1B9A','대시보드 + 마법사형','대시보드 + 통계 + 카테고리 탭 + 위자드','메인: 대시보드 + 피드','입력: 단계별 마법사 (1→2→3)'),
    ]
    cw,ch=138,182; gap=12
    total3=3*cw+2*gap; csx=(W-total3)/2
    csy=H*.634-ch-20

    for i,(aid,ac,aname,adesc,l1,l2) in enumerate(cards):
        cx=csx+i*(cw+gap)
        rr(c,cx,csy,cw,ch,12,fill='#FFFFFF0C')
        c.setStrokeColor(HexColor(ac)); c.setLineWidth(1.5)
        c.roundRect(cx,csy,cw,ch,12,fill=0,stroke=1)
        # color top
        rr(c,cx+8,csy+ch-46,cw-16,36,8,fill=ac)
        t(c,aid,cx+cw//2,csy+ch-28,sz=20,bold=True,col='#FFFFFF',align='center')
        # name
        t(c,aname,cx+cw//2,csy+ch-60,sz=8,bold=True,col='#FFFFFF',align='center')
        # desc lines
        t(c,l1,cx+10,csy+ch-78,sz=6.5,col='#AABBCC')
        t(c,l2,cx+10,csy+ch-91,sz=6.5,col='#AABBCC')
        t(c,adesc,cx+10,csy+ch-108,sz=6,col='#667788')
        # page link
        rr(c,cx+cw//2-22,csy+8,44,18,9,fill=ac+'30')
        c.setStrokeColor(HexColor(ac)); c.setLineWidth(0.8)
        c.roundRect(cx+cw//2-22,csy+8,44,18,9,fill=0,stroke=1)
        t(c,f'p.{i+2}  →',cx+cw//2,csy+13,sz=7.5,col=ac,align='center')

    t(c,'레이아웃 구조 · 인터랙션 패턴 · 입력 UX가 각각 다릅니다.',W//2,csy-28,sz=9,col='#556677',align='center')

# ────────────────────────────────────────────────────────────────────────────

pdf = pdfmod.Canvas(OUTPUT, pagesize=A4)
draw_title(pdf); pdf.showPage()
for info in DESIGNS:
    draw_design_page(pdf, info); pdf.showPage()
pdf.save()
print(f"Generated: {OUTPUT}")
