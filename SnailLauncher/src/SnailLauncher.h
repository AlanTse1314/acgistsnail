
// SnailLauncher.h : SnailLauncher Ӧ�ó������ͷ�ļ�
//
#pragma once

#ifndef __AFXWIN_H__
	#error "�ڰ������ļ�֮ǰ������stdafx.h�������� PCH �ļ�"
#endif

// ������
#include "resource.h"

class SnailLauncher : public CWinApp
{
public:
	SnailLauncher();

// ��д
public:
	virtual BOOL InitInstance();
	virtual int ExitInstance();

// ʵ��
public:
	DECLARE_MESSAGE_MAP();
};

extern SnailLauncher launcher;
