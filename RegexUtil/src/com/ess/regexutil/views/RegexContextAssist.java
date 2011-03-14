package com.ess.regexutil.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class RegexContextAssist extends ContentAssistant {

	private final StyledText regexEditor;
	
	public RegexContextAssist(StyledText regexEditor) {
		this.regexEditor = regexEditor;
		setContentAssistProcessor(new RegexContextAsssistProcessor(), "");
		install(new StyleTextContentAssistSubjectControl(regexEditor));
	}
	
	private class RegexContextAsssistProcessor implements IContentAssistProcessor {

		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
			return new ICompletionProposal[]{
					new ICompletionProposal() {

						public void apply(IDocument document) {
							
						}

						public String getAdditionalProposalInfo() {
							return "getAdditionalProposalInfo";
						}

						public IContextInformation getContextInformation() {
							return null;
						}

						public String getDisplayString() {
							return "getDisplayString";
						}

						public Image getImage() {
							// TODO Auto-generated method stub
							return null;
						}

						public Point getSelection(IDocument document) {
							// TODO Auto-generated method stub
							return null;
						}
						
					}
			};
		}

		public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
			// TODO Auto-generated method stub
			return null;
		}

		public char[] getCompletionProposalAutoActivationCharacters() {
			// TODO Auto-generated method stub
			return null;
		}

		public char[] getContextInformationAutoActivationCharacters() {
			// TODO Auto-generated method stub
			return null;
		}

		public IContextInformationValidator getContextInformationValidator() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getErrorMessage() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
