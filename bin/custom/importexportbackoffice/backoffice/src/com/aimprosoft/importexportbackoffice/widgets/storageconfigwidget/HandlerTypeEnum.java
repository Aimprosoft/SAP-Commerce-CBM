package com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget;

public enum HandlerTypeEnum
{
	CONFIG_SELECT("ConfigSelect"), TYPE_SELECT("TypeSelect"), CONNECT("Connect"), DISCONNECT("Disconnect");

	String typeCode;

	HandlerTypeEnum(String typeCode)
	{
		this.typeCode = typeCode;
	}

	public String getTypeCode()
	{
		return typeCode;
	}
}
