#-*- coding: utf-8 -*-

'''
Create by ZhuXuan
Use opnecv to cut picture then use baidu ORC API convert to words
'''

import numpy as np
import cv2
from matplotlib import pyplot as plt

from aip import AipOcr
import re

def segement(path,save_path):
	img = cv2.imread(path)

	gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
	thresh = cv2.adaptiveThreshold(gray,255,cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY_INV,55,7)
	image ,contours,hierarchy = cv2.findContours(thresh,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)

	c = sorted(contours, key=cv2.contourArea, reverse=True)[0]
	rect = cv2.minAreaRect(c)
	box = np.int0(cv2.boxPoints(rect))

	Xs = [i[0] for i in box]
	Ys = [i[1] for i in box]
	x1 = min(Xs)
	x2 = max(Xs)
	y1 = min(Ys)
	y2 = max(Ys)
	hight = y2 - y1
	width = x2 - x1

	max_index = -1
	max_area = -1
	for con in range(len(contours)):
		cnt = contours[con]
		area = cv2.contourArea(cnt)
		if area > max_area:
			max_area = area
			max_index = con

	# cut the picture
	mask = np.zeros(img.shape[:2],np.uint8)
	cv2.drawContours(mask, contours, max_index, (255,255,255), cv2.FILLED)

	image=cv2.add(img, np.zeros(np.shape(img), dtype=np.uint8), mask=mask)
	result_img = image[y1:y2, x1:x2]
	
	cv2.imwrite(save_path, result_img)
	
def pic2word(path):
	from . import const
	
	# put your API key here
	client = AipOcr(const.APP_ID,const.API_KEY,const.SECRECT_KEY)
	i = open(path,'rb')
	img = i.read()
	message = client.basicGeneral(img)
	return message
