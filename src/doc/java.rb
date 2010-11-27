
def ex( citation, suffix='', atts='' )
	res = ''
	res << '<notextile><pre class="jc">[j<b></b>c' + suffix + ':' + citation + ']</pre></notextile>'
	res << "\n"
	res << '<pre' + atts + '>[jc' + suffix + ':' + citation + ']</pre>'
	res
end

