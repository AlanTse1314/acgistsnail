// SnailLauncher.h : SnailLauncher Ӧ�ó������ͷ�ļ�
//

#pragma once

// ������
#include "resource.h"

class SnailLauncher : public CWinApp {
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
